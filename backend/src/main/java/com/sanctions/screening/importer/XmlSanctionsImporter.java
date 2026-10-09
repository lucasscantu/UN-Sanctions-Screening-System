package com.sanctions.screening.importer;

import com.sanctions.screening.config.SanctionsConfig;
import com.sanctions.screening.entity.*;
import com.sanctions.screening.repository.*;
import com.sanctions.screening.util.NameNormalizer;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class XmlSanctionsImporter {

    private final SanctionedRecordRepository sanctionedRecordRepository;
    private final NameAliasRepository nameAliasRepository;
    private final BiographicalDetailRepository biographicalDetailRepository;
    private final SanctionsProgramRepository sanctionsProgramRepository;
    private final AddressRepository addressRepository;
    private final IdentifierRepository identifierRepository;
    private final ImportJobRepository importJobRepository;
    private final SanctionsConfig.SanctionsProperties sanctionsProperties;
    private final NameNormalizer nameNormalizer;

    private final Lock importLock = new ReentrantLock();
    private volatile boolean importInProgress = false;

    private static final String CONSOLIDATED_LIST_URL = "https://unsolprodfiles.blob.core.windows.net/publiclegacyxmlfiles/EN/consolidated.xml";

    @Transactional
    public ImportResult importSanctionsList() {
        return importSanctionsList(null);
    }

    @Transactional
    public ImportResult importSanctionsList(String triggeredBy) {
        if (!importLock.tryLock()) {
            log.warn("Import already in progress, skipping concurrent import request");
            return ImportResult.builder()
                    .success(false)
                    .message("Import already in progress")
                    .build();
        }

        try {
            importInProgress = true;
            String jobUuid = UUID.randomUUID().toString();
            LocalDateTime startTime = LocalDateTime.now();

            ImportJob job = ImportJob.builder()
                    .jobUuid(jobUuid)
                    .sourceUrl(getSourceUrl())
                    .status("IN_PROGRESS")
                    .startedAt(startTime)
                    .isManualTrigger(triggeredBy != null)
                    .triggeredBy(triggeredBy)
                    .build();
            importJobRepository.save(job);

            log.info("Starting UN sanctions list import from: {}", getSourceUrl());

            // Download XML
            byte[] xmlData;
            try {
                xmlData = downloadXml(getSourceUrl(), sanctionsProperties.downloadTimeoutSeconds());
                job.setFileSizeBytes(xmlData.length);
                job.setFileChecksum(generateChecksum(xmlData));
            } catch (Exception e) {
                log.error("Failed to download XML: {}", e.getMessage());
                job.setStatus("FAILED");
                job.setErrorSummary("Download failed: " + e.getMessage());
                job.setCompletedAt(LocalDateTime.now());
                importJobRepository.save(job);
                return ImportResult.builder()
                        .success(false)
                        .message("Failed to download XML: " + e.getMessage())
                        .build();
            }

            // Parse XML
            ConsolidatedList consolidatedList;
            try {
                consolidatedList = parseXml(xmlData);
            } catch (Exception e) {
                log.error("Failed to parse XML: {}", e.getMessage());
                job.setStatus("FAILED");
                job.setErrorSummary("Parse failed: " + e.getMessage());
                job.setCompletedAt(LocalDateTime.now());
                importJobRepository.save(job);
                return ImportResult.builder()
                        .success(false)
                        .message("Failed to parse XML: " + e.getMessage())
                        .build();
            }

            // Process records
            int created = 0, updated = 0, failed = 0;
            int totalProcessed = 0;

            // Process individuals
            if (consolidatedList.getIndividuals() != null) {
                for (Individual individual : consolidatedList.getIndividuals().getIndividuals()) {
                    try {
                        processIndividual(individual, job);
                        totalProcessed++;
                        if (individual.getPersonId() != null && individual.getPersonId().getUniqueId() != null) {
                            created++;
                        } else {
                            updated++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to process individual: {}", e.getMessage());
                        failed++;
                    }
                }
            }

            // Process entities
            if (consolidatedList.getEntities() != null) {
                for (Entity entity : consolidatedList.getEntities().getEntities()) {
                    try {
                        processEntity(entity, job);
                        totalProcessed++;
                        if (entity.getEntityId() != null && entity.getEntityId().getUniqueId() != null) {
                            created++;
                        } else {
                            updated++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to process entity: {}", e.getMessage());
                        failed++;
                    }
                }
            }

            // Update job statistics
            job.setRecordsCreated(created);
            job.setRecordsUpdated(updated);
            job.setRecordsFailed(failed);
            job.setTotalRecordsProcessed(totalProcessed);
            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());
            importJobRepository.save(job);

            log.info("Import completed: {} records created, {} updated, {} failed", 
                    created, updated, failed);

            return ImportResult.builder()
                    .success(true)
                    .message("Import completed successfully")
                    .recordsCreated(created)
                    .recordsUpdated(updated)
                    .recordsFailed(failed)
                    .jobUuid(jobUuid)
                    .build();

        } catch (Exception e) {
            log.error("Unexpected error during import: {}", e.getMessage(), e);
            return ImportResult.builder()
                    .success(false)
                    .message("Unexpected error: " + e.getMessage())
                    .build();
        } finally {
            importInProgress = false;
            importLock.unlock();
        }
    }

    private String getSourceUrl() {
        return sanctionsProperties.xmlSourceUrl();
    }

    private byte[] downloadXml(String url, int timeoutSeconds) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Accept", "application/xml")
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " - " + response.body());
        }

        return response.body();
    }

    private ConsolidatedList parseXml(byte[] xmlData) throws Exception {
        try {
            // Create secure XML parser
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlData));

            // Use JAXB for unmarshalling
            JAXBContext context = JAXBContext.newInstance(ConsolidatedList.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            return (ConsolidatedList) unmarshaller.unmarshal(new ByteArrayInputStream(xmlData));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new Exception("XML parsing error: " + e.getMessage(), e);
        }
    }

    @Transactional
    protected void processIndividual(Individual individual, ImportJob job) {
        String referenceNumber = extractReferenceNumber(individual);
        
        SanctionedRecord existingRecord = sanctionedRecordRepository.findByUnReferenceNumber(referenceNumber)
                .orElse(null);

        SanctionedRecord record;
        if (existingRecord != null) {
            record = existingRecord;
            record.setLastSyncedAt(LocalDateTime.now());
        } else {
            record = SanctionedRecord.builder()
                    .unReferenceNumber(referenceNumber)
                    .recordType(RecordType.INDIVIDUAL)
                    .firstImportedAt(LocalDateTime.now())
                    .lastSyncedAt(LocalDateTime.now())
                    .isActive(true)
                    .sourceUrl(getSourceUrl())
                    .build();
        }

        // Set primary name
        String primaryName = extractPrimaryName(individual);
        record.setPrimaryName(primaryName);
        record.setPrimaryNameNormalized(nameNormalizer.normalize(primaryName));

        // Set submitted name
        if (individual.getSubmittedName() != null) {
            record.setSubmittedName(individual.getSubmittedName());
        }

        // Set sort key
        if (individual.getSortKey() != null) {
            record.setSortKey(individual.getSortKey());
        }

        // Set designation
        if (individual.getDesignation() != null) {
            record.setDesignation(individual.getDesignation());
        }

        // Set listed on date
        if (individual.getListedOn() != null) {
            try {
                record.setListedOn(java.time.LocalDate.parse(individual.getListedOn()));
            } catch (Exception e) {
                log.warn("Failed to parse listed on date: {}", individual.getListedOn());
            }
        }

        // Process person ID
        PersonId personId = individual.getPersonId();
        if (personId != null) {
            // Additional name components can be extracted here
            if (personId.getIndividualTitle() != null) {
                // Title is part of the name
            }
        }

        // Process aliases
        processAliases(individual.getIndividualAlias(), record);

        // Process date of birth
        processDateOfBirth(individual.getIndividualDateOfBirth(), record);

        // Process place of birth
        processPlaceOfBirth(individual.getIndividualPlaceOfBirth(), record);

        // Process nationality
        processNationality(individual.getIndividualNationality(), record);

        // Process addresses
        processAddresses(individual.getIndividualAddress(), record);

        // Process sanctions programs
        processSanctionsPrograms(individual.getCommittee(), individual.getOtherInformation(), record);

        // Save record
        sanctionedRecordRepository.save(record);
        
        // Store raw XML for audit
        try {
            JAXBContext context = JAXBContext.newInstance(Individual.class);
            var marshaller = context.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(individual, writer);
            record.setRawXmlData(writer.toString());
        } catch (Exception e) {
            log.debug("Failed to store raw XML for individual: {}", e.getMessage());
        }
    }

    @Transactional
    protected void processEntity(Entity entity, ImportJob job) {
        String referenceNumber = extractReferenceNumber(entity);
        
        SanctionedRecord existingRecord = sanctionedRecordRepository.findByUnReferenceNumber(referenceNumber)
                .orElse(null);

        SanctionedRecord record;
        if (existingRecord != null) {
            record = existingRecord;
            record.setLastSyncedAt(LocalDateTime.now());
        } else {
            record = SanctionedRecord.builder()
                    .unReferenceNumber(referenceNumber)
                    .recordType(RecordType.ENTITY)
                    .firstImportedAt(LocalDateTime.now())
                    .lastSyncedAt(LocalDateTime.now())
                    .isActive(true)
                    .sourceUrl(getSourceUrl())
                    .build();
        }

        // Set primary name
        String primaryName = extractPrimaryName(entity);
        record.setPrimaryName(primaryName);
        record.setPrimaryNameNormalized(nameNormalizer.normalize(primaryName));

        // Set submitted name
        if (entity.getSubmittedName() != null) {
            record.setSubmittedName(entity.getSubmittedName());
        }

        // Set sort key
        if (entity.getSortKey() != null) {
            record.setSortKey(entity.getSortKey());
        }

        // Set listed on date
        if (entity.getListedOn() != null) {
            try {
                record.setListedOn(java.time.LocalDate.parse(entity.getListedOn()));
            } catch (Exception e) {
                log.warn("Failed to parse listed on date: {}", entity.getListedOn());
            }
        }

        // Process entity aliases
        processEntityAliases(entity.getEntityAlias(), record);

        // Process entity addresses
        processEntityAddresses(entity.getEntityAddress(), record);

        // Process sanctions programs
        processEntitySanctionsPrograms(entity.getCommittee(), entity.getOtherInformation(), record);

        // Save record
        sanctionedRecordRepository.save(record);

        // Store raw XML for audit
        try {
            JAXBContext context = JAXBContext.newInstance(Entity.class);
            var marshaller = context.createMarshaller();
            StringWriter writer = new StringWriter();
            marshaller.marshal(entity, writer);
            record.setRawXmlData(writer.toString());
        } catch (Exception e) {
            log.debug("Failed to store raw XML for entity: {}", e.getMessage());
        }
    }

    private String extractReferenceNumber(Individual individual) {
        if (individual.getReferenceNumber() != null && !individual.getReferenceNumber().isEmpty()) {
            return individual.getReferenceNumber();
        }
        if (individual.getPersonId() != null && individual.getPersonId().getUniqueId() != null) {
            return "UNSC-IND-" + individual.getPersonId().getUniqueId();
        }
        return "UNSC-IND-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String extractReferenceNumber(Entity entity) {
        if (entity.getReferenceNumber() != null && !entity.getReferenceNumber().isEmpty()) {
            return entity.getReferenceNumber();
        }
        if (entity.getEntityId() != null && entity.getEntityId().getUniqueId() != null) {
            return "UNSC-ENT-" + entity.getEntityId().getUniqueId();
        }
        return "UNSC-ENT-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String extractPrimaryName(Individual individual) {
        if (individual.getPersonId() != null) {
            StringBuilder name = new StringBuilder();
            PersonId pid = individual.getPersonId();
            
            if (pid.getFirstName() != null && !pid.getFirstName().isEmpty()) {
                name.append(pid.getFirstName()).append(" ");
            }
            if (pid.getSecondName() != null && !pid.getSecondName().isEmpty()) {
                name.append(pid.getSecondName()).append(" ");
            }
            if (pid.getThirdName() != null && !pid.getThirdName().isEmpty()) {
                name.append(pid.getThirdName()).append(" ");
            }
            if (pid.getFourthName() != null && !pid.getFourthName().isEmpty()) {
                name.append(pid.getFourthName()).append(" ");
            }
            
            String result = name.toString().trim();
            if (!result.isEmpty()) {
                return result;
            }
        }
        
        if (individual.getSubmittedName() != null && !individual.getSubmittedName().isEmpty()) {
            return individual.getSubmittedName();
        }
        
        return "UNKNOWN";
    }

    private String extractPrimaryName(Entity entity) {
        if (entity.getEntityId() != null && entity.getEntityId().getEntityName() != null) {
            return entity.getEntityId().getEntityName();
        }
        
        if (entity.getSubmittedName() != null && !entity.getSubmittedName().isEmpty()) {
            return entity.getSubmittedName();
        }
        
        return "UNKNOWN";
    }

    private void processAliases(List<IndividualAlias> aliases, SanctionedRecord record) {
        // Clear existing aliases
        nameAliasRepository.deleteBySanctionedRecordId(record.getId());
        
        if (aliases == null || aliases.isEmpty()) {
            return;
        }

        for (IndividualAlias alias : aliases) {
            NameAlias nameAlias = NameAlias.builder()
                    .sanctionedRecord(record)
                    .aliasName(alias.getAliasName())
                    .aliasNameNormalized(nameNormalizer.normalize(alias.getAliasName()))
                    .nameOriginDesc(alias.getNameOriginDesc())
                    .quality(mapAliasQuality(alias.getQuality()))
                    .isPrimaryAlias("Primary name".equalsIgnoreCase(alias.getNameOriginDesc()))
                    .build();
            
            nameAliasRepository.save(nameAlias);
            record.addAlias(nameAlias);
        }
    }

    private void processEntityAliases(List<EntityAlias> aliases, SanctionedRecord record) {
        // Clear existing aliases
        nameAliasRepository.deleteBySanctionedRecordId(record.getId());
        
        if (aliases == null || aliases.isEmpty()) {
            return;
        }

        for (EntityAlias alias : aliases) {
            NameAlias nameAlias = NameAlias.builder()
                    .sanctionedRecord(record)
                    .aliasName(alias.getAliasName())
                    .aliasNameNormalized(nameNormalizer.normalize(alias.getAliasName()))
                    .nameOriginDesc(alias.getNameOriginDesc())
                    .quality(mapAliasQuality(alias.getQuality()))
                    .isPrimaryAlias("Primary name".equalsIgnoreCase(alias.getNameOriginDesc()))
                    .build();
            
            nameAliasRepository.save(nameAlias);
            record.addAlias(nameAlias);
        }
    }

    private AliasQuality mapAliasQuality(String quality) {
        if (quality == null) {
            return AliasQuality.UNKNOWN;
        }
        return switch (quality.toUpperCase()) {
            case "GOOD" -> AliasQuality.GOOD;
            case "LOW" -> AliasQuality.LOW;
            default -> AliasQuality.UNKNOWN;
        };
    }

    private void processDateOfBirth(List<IndividualDateOfBirth> dobList, SanctionedRecord record) {
        biographicalDetailRepository.deleteBySanctionedRecordId(record.getId());
        
        if (dobList == null || dobList.isEmpty()) {
            return;
        }

        for (IndividualDateOfBirth dob : dobList) {
            BiographicalDetail detail = BiographicalDetail.builder()
                    .sanctionedRecord(record)
                    .dateOfBirth(parseDate(dob.getDate()))
                    .datePrecision(mapDatePrecision(dob.getPrecision()))
                    .build();
            
            biographicalDetailRepository.save(detail);
            record.addBiographicalDetail(detail);
        }
    }

    private void processPlaceOfBirth(List<IndividualPlaceOfBirth> pobList, SanctionedRecord record) {
        if (pobList == null || pobList.isEmpty()) {
            return;
        }

        // Get existing detail or create new
        BiographicalDetail detail = biographicalDetailRepository.findBySanctionedRecordId(record.getId())
                .stream().findFirst().orElse(null);
        
        if (detail == null && !record.getBiographicalDetails().isEmpty()) {
            detail = record.getBiographicalDetails().iterator().next();
        }
        
        for (IndividualPlaceOfBirth pob : pobList) {
            if (detail == null) {
                detail = BiographicalDetail.builder()
                        .sanctionedRecord(record)
                        .build();
                biographicalDetailRepository.save(detail);
                record.addBiographicalDetail(detail);
            }
            
            if (pob.getCity() != null) {
                detail.setCityOfBirth(pob.getCity());
            }
            if (pob.getCountry() != null) {
                detail.setCountryOfBirth(pob.getCountry());
            }
            if (pob.getPlaceOfBirth() != null) {
                detail.setPlaceOfBirth(pob.getPlaceOfBirth());
            }
            
            biographicalDetailRepository.save(detail);
        }
    }

    private void processNationality(List<IndividualNationality> nationalityList, SanctionedRecord record) {
        if (nationalityList == null || nationalityList.isEmpty()) {
            return;
        }

        BiographicalDetail detail = biographicalDetailRepository.findBySanctionedRecordId(record.getId())
                .stream().findFirst().orElse(null);
        
        if (detail == null && !record.getBiographicalDetails().isEmpty()) {
            detail = record.getBiographicalDetails().iterator().next();
        }
        
        StringBuilder nationalities = new StringBuilder();
        for (IndividualNationality nat : nationalityList) {
            if (nat.getCountry() != null && !nat.getCountry().isEmpty()) {
                if (nationalities.length() > 0) {
                    nationalities.append(", ");
                }
                nationalities.append(nat.getCountry());
            }
        }
        
        if (nationalities.length() > 0) {
            if (detail == null) {
                detail = BiographicalDetail.builder()
                        .sanctionedRecord(record)
                        .build();
                biographicalDetailRepository.save(detail);
                record.addBiographicalDetail(detail);
            }
            detail.setNationality(nationalities.toString());
            biographicalDetailRepository.save(detail);
        }
    }

    private void processAddresses(List<IndividualAddress> addressList, SanctionedRecord record) {
        addressRepository.deleteBySanctionedRecordId(record.getId());
        
        if (addressList == null || addressList.isEmpty()) {
            return;
        }

        for (IndividualAddress address : addressList) {
            Address addr = Address.builder()
                    .sanctionedRecord(record)
                    .street(address.getStreet())
                    .city(address.getCity())
                    .stateOrProvince(address.getStateOrProvince())
                    .postalCode(address.getPostalCode())
                    .country(address.getCountry())
                    .addressType(address.getAddressType())
                    .build();
            
            addressRepository.save(addr);
            record.addAddress(addr);
        }
    }

    private void processEntityAddresses(List<EntityAddress> addressList, SanctionedRecord record) {
        addressRepository.deleteBySanctionedRecordId(record.getId());
        
        if (addressList == null || addressList.isEmpty()) {
            return;
        }

        for (EntityAddress address : addressList) {
            Address addr = Address.builder()
                    .sanctionedRecord(record)
                    .street(address.getStreet())
                    .city(address.getCity())
                    .stateOrProvince(address.getStateOrProvince())
                    .postalCode(address.getPostalCode())
                    .country(address.getCountry())
                    .addressType(address.getAddressType())
                    .build();
            
            addressRepository.save(addr);
            record.addAddress(addr);
        }
    }

    private void processSanctionsPrograms(String committee, String otherInformation, SanctionedRecord record) {
        sanctionsProgramRepository.deleteBySanctionedRecordId(record.getId());
        
        if (committee == null || committee.isEmpty()) {
            return;
        }

        SanctionsProgram program = SanctionsProgram.builder()
                .sanctionedRecord(record)
                .committee(committee)
                .programName("UNSC " + committee)
                .otherInformation(otherInformation)
                .build();
        
        sanctionsProgramRepository.save(program);
        record.addSanctionsProgram(program);
    }

    private void processEntitySanctionsPrograms(String committee, String otherInformation, SanctionedRecord record) {
        sanctionsProgramRepository.deleteBySanctionedRecordId(record.getId());
        
        if (committee == null || committee.isEmpty()) {
            return;
        }

        SanctionsProgram program = SanctionsProgram.builder()
                .sanctionedRecord(record)
                .committee(committee)
                .programName("UNSC " + committee)
                .otherInformation(otherInformation)
                .build();
        
        sanctionsProgramRepository.save(program);
        record.addSanctionsProgram(program);
    }

    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            // Try ISO format
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e) {
            log.debug("Failed to parse date: {}", dateStr);
            return null;
        }
    }

    private DatePrecision mapDatePrecision(String precision) {
        if (precision == null) {
            return DatePrecision.UNKNOWN;
        }
        return switch (precision.toUpperCase()) {
            case "EXACT" -> DatePrecision.EXACT;
            case "APPROXIMATE" -> DatePrecision.APPROXIMATE;
            case "YEAR_ONLY" -> DatePrecision.YEAR_ONLY;
            case "MONTH_AND_YEAR" -> DatePrecision.MONTH_AND_YEAR;
            default -> DatePrecision.UNKNOWN;
        };
    }

    private String generateChecksum(byte[] data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isImportInProgress() {
        return importInProgress;
    }

    public record ImportResult(
            boolean success,
            String message,
            int recordsCreated,
            int recordsUpdated,
            int recordsFailed,
            String jobUuid
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private boolean success;
            private String message;
            private int recordsCreated;
            private int recordsUpdated;
            private int recordsFailed;
            private String jobUuid;

            public Builder success(boolean success) {
                this.success = success;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder recordsCreated(int recordsCreated) {
                this.recordsCreated = recordsCreated;
                return this;
            }

            public Builder recordsUpdated(int recordsUpdated) {
                this.recordsUpdated = recordsUpdated;
                return this;
            }

            public Builder recordsFailed(int recordsFailed) {
                this.recordsFailed = recordsFailed;
                return this;
            }

            public Builder jobUuid(String jobUuid) {
                this.jobUuid = jobUuid;
                return this;
            }

            public ImportResult build() {
                return new ImportResult(
                        success, message, recordsCreated, recordsUpdated, recordsFailed, jobUuid
                );
            }
        }
    }
}
