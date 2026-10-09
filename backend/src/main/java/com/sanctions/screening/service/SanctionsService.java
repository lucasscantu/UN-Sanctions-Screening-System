package com.sanctions.screening.service;

import com.sanctions.screening.dto.*;
import com.sanctions.screening.entity.*;
import com.sanctions.screening.importer.XmlSanctionsImporter;
import com.sanctions.screening.importer.XmlSanctionsImporter.ImportResult;
import com.sanctions.screening.matching.SearchService;
import com.sanctions.screening.matching.SimilarityCalculator;
import com.sanctions.screening.ollama.OllamaService;
import com.sanctions.screening.ollama.OllamaService.NameAnalysisRequest;
import com.sanctions.screening.ollama.OllamaService.OllamaResponse;
import com.sanctions.screening.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SanctionsService {

    private final SanctionedRecordRepository sanctionedRecordRepository;
    private final ImportJobRepository importJobRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final NameAliasRepository nameAliasRepository;
    private final XmlSanctionsImporter xmlSanctionsImporter;
    private final SearchService searchService;
    private final OllamaService ollamaService;
    private final SimilarityCalculator similarityCalculator;

    // Search operations
    public SearchResult searchSanctions(SearchRequest request) {
        SearchResult result = searchService.search(request);
        
        // Record search history
        recordSearch(request, result);
        
        return result;
    }

    private void recordSearch(SearchRequest request, SearchResult result) {
        SearchHistory history = SearchHistory.builder()
                .searchQuery(request.getName())
                .searchQueryNormalized(request.getName() != null ? 
                        new com.sanctions.screening.util.NameNormalizer().normalize(request.getName()) : null)
                .recordTypeFilter(request.getRecordType())
                .resultsCount(result.getTotalResults())
                .minScoreFilter(request.getMinScore() != null ? request.getMinScore() : 0.0)
                .searchDurationMs(result.getSearchDurationMs())
                .aiAnalysisEnabled(false)
                .userAgent("API")
                .createdAt(LocalDateTime.now())
                .build();
        
        searchHistoryRepository.save(history);
    }

    // Record operations
    public Optional<SanctionedRecordDto> getRecordById(Long id) {
        return sanctionedRecordRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<SanctionedRecordDto> getRecordByReference(String referenceNumber) {
        return sanctionedRecordRepository.findByUnReferenceNumber(referenceNumber)
                .map(this::convertToDto);
    }

    public Page<SanctionedRecordDto> getAllRecords(Pageable pageable) {
        Page<SanctionedRecord> records = sanctionedRecordRepository.findAllActive(pageable);
        List<SanctionedRecordDto> dtos = records.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, records.getTotalElements());
    }

    // Import operations
    @Transactional
    public ImportResult triggerImport(String triggeredBy) {
        return xmlSanctionsImporter.importSanctionsList(triggeredBy);
    }

    public List<ImportJobDto> getImportHistory() {
        return importJobRepository.findTop10ByOrderByStartedAtDesc().stream()
                .map(this::convertImportJobToDto)
                .collect(Collectors.toList());
    }

    public Optional<ImportJobDto> getImportJob(String jobUuid) {
        return importJobRepository.findByJobUuid(jobUuid)
                .map(this::convertImportJobToDto);
    }

    public ImportStatusDto getImportStatus() {
        ImportJob lastJob = importJobRepository.findTopByStatusOrderByStartedAtDesc("IN_PROGRESS")
                .orElse(null);
        
        long totalRecords = sanctionedRecordRepository.countByIsActive(true);
        long totalIndividuals = sanctionedRecordRepository.countByRecordType(RecordType.INDIVIDUAL);
        long totalEntities = sanctionedRecordRepository.countByRecordType(RecordType.ENTITY);

        return ImportStatusDto.builder()
                .importInProgress(xmlSanctionsImporter.isImportInProgress())
                .lastImportJob(lastJob != null ? convertImportJobToDto(lastJob) : null)
                .totalRecords(totalRecords)
                .totalIndividuals(totalIndividuals)
                .totalEntities(totalEntities)
                .build();
    }

    // AI Analysis operations
    @Async
    public CompletableFuture<List<AIAnalysisResult>> analyzeWithAI(List<SearchResult.ResultItem> results) {
        List<CompletableFuture<AIAnalysisResult>> futures = new ArrayList<>();
        
        for (SearchResult.ResultItem result : results) {
            futures.add(analyzeSingleWithAI(result));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<AIAnalysisResult> analyzeSingleWithAI(SearchResult.ResultItem result) {
        NameAnalysisRequest request = new NameAnalysisRequest(
                result.getPrimaryName(),
                result.getPrimaryName(),
                result.getAliases(),
                result.getRecordType().name(),
                result.getUnReferenceNumber()
        );

        return ollamaService.analyzeNameSimilarity(request)
                .map(ollamaResponse -> {
                    if (ollamaResponse.success() && ollamaResponse.analysis() != null) {
                        return AIAnalysisResult.builder()
                                .recordId(result.getRecordId())
                                .assessment(ollamaResponse.analysis().assessment())
                                .explanation(ollamaResponse.analysis().explanation())
                                .linguisticVariants(convertLinguisticVariants(ollamaResponse.analysis().linguisticVariants()))
                                .matchingEvidence(convertMatchingEvidence(ollamaResponse.analysis().matchingEvidence()))
                                .conflictingEvidence(convertConflictingEvidence(ollamaResponse.analysis().conflictingEvidence()))
                                .warning(ollamaResponse.analysis().warning())
                                .disclaimer(ollamaResponse.analysis().disclaimer())
                                .modelUsed(ollamaResponse.modelUsed())
                                .build();
                    } else {
                        return AIAnalysisResult.builder()
                                .recordId(result.getRecordId())
                                .assessment("INDETERMINATE")
                                .explanation("AI analysis unavailable: " + (ollamaResponse.error() != null ? ollamaResponse.error() : "Unknown error"))
                                .warning("AI analysis failed")
                                .build();
                    }
                })
                .toFuture();
    }

    // Helper methods
    private SanctionedRecordDto convertToDto(SanctionedRecord record) {
        List<NameAliasDto> aliasDtos = nameAliasRepository.findBySanctionedRecordId(record.getId()).stream()
                .map(alias -> NameAliasDto.builder()
                        .id(alias.getId())
                        .aliasName(alias.getAliasName())
                        .quality(alias.getQuality() != null ? alias.getQuality().name() : null)
                        .nameOriginDesc(alias.getNameOriginDesc())
                        .build())
                .collect(Collectors.toList());

        return SanctionedRecordDto.builder()
                .id(record.getId())
                .unReferenceNumber(record.getUnReferenceNumber())
                .recordType(record.getRecordType())
                .primaryName(record.getPrimaryName())
                .submittedName(record.getSubmittedName())
                .sortKey(record.getSortKey())
                .designation(record.getDesignation())
                .listedOn(record.getListedOn())
                .sourceUrl(record.getSourceUrl())
                .firstImportedAt(record.getFirstImportedAt())
                .lastSyncedAt(record.getLastSyncedAt())
                .isActive(record.isActive())
                .sourceStatus(record.getSourceStatus())
                .aliases(aliasDtos)
                .build();
    }

    private ImportJobDto convertImportJobToDto(ImportJob job) {
        return ImportJobDto.builder()
                .id(job.getId())
                .jobUuid(job.getJobUuid())
                .sourceUrl(job.getSourceUrl())
                .status(job.getStatus())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .recordsCreated(job.getRecordsCreated())
                .recordsUpdated(job.getRecordsUpdated())
                .recordsDeactivated(job.getRecordsDeactivated())
                .recordsFailed(job.getRecordsFailed())
                .totalRecordsProcessed(job.getTotalRecordsProcessed())
                .errorSummary(job.getErrorSummary())
                .fileSizeBytes(job.getFileSizeBytes())
                .isManualTrigger(job.isManualTrigger())
                .triggeredBy(job.getTriggeredBy())
                .build();
    }

    private AIAnalysisResult.LinguisticVariantsDto convertLinguisticVariants(
            OllamaService.NameAnalysisResult.LinguisticVariants variants) {
        return AIAnalysisResult.LinguisticVariantsDto.builder()
                .transliterationVariants(variants.transliterationVariants())
                .spellingVariants(variants.spellingVariants())
                .nameOrderDifferences(variants.nameOrderDifferences())
                .abbreviationDifferences(variants.abbreviationDifferences())
                .build();
    }

    private AIAnalysisResult.MatchingEvidenceDto convertMatchingEvidence(
            OllamaService.NameAnalysisResult.MatchingEvidence evidence) {
        return AIAnalysisResult.MatchingEvidenceDto.builder()
                .exactMatch(evidence.exactMatch())
                .partialMatch(evidence.partialMatch())
                .aliasMatch(evidence.aliasMatch())
                .bestMatchingAlias(evidence.bestMatchingAlias())
                .matchingAliases(evidence.matchingAliases())
                .build();
    }

    private AIAnalysisResult.ConflictingEvidenceDto convertConflictingEvidence(
            OllamaService.NameAnalysisResult.ConflictingEvidence evidence) {
        return AIAnalysisResult.ConflictingEvidenceDto.builder()
                .differentNames(evidence.differentNames())
                .missingInformation(evidence.missingInformation())
                .build();
    }
}
