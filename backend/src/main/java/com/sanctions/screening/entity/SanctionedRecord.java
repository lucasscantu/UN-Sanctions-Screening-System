package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sanctioned_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionedRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "un_reference_number", unique = true, nullable = false)
    private String unReferenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private RecordType recordType;

    @Column(name = "primary_name", nullable = false, length = 1000)
    private String primaryName;

    @Column(name = "primary_name_normalized", length = 1000)
    private String primaryNameNormalized;

    @Column(name = "submitted_name", length = 1000)
    private String submittedName;

    @Column(name = "sort_key", length = 1000)
    private String sortKey;

    @Column(name = "designation", length = 2000)
    private String designation;

    @Column(name = "listed_on")
    private LocalDate listedOn;

    @Column(name = "source_url", length = 2000)
    private String sourceUrl;

    @Column(name = "first_imported_at")
    @CreationTimestamp
    private LocalDateTime firstImportedAt;

    @Column(name = "last_synced_at")
    @UpdateTimestamp
    private LocalDateTime lastSyncedAt;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "source_status", length = 100)
    private String sourceStatus;

    @Column(name = "raw_xml_data", columnDefinition = "TEXT")
    private String rawXmlData;

    @OneToMany(mappedBy = "sanctionedRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<NameAlias> aliases = new HashSet<>();

    @OneToMany(mappedBy = "sanctionedRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BiographicalDetail> biographicalDetails = new HashSet<>();

    @OneToMany(mappedBy = "sanctionedRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SanctionsProgram> sanctionsPrograms = new HashSet<>();

    @OneToMany(mappedBy = "sanctionedRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "sanctionedRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Identifier> identifiers = new HashSet<>();

    // Add alias
    public void addAlias(NameAlias alias) {
        aliases.add(alias);
        alias.setSanctionedRecord(this);
    }

    // Add biographical detail
    public void addBiographicalDetail(BiographicalDetail detail) {
        biographicalDetails.add(detail);
        detail.setSanctionedRecord(this);
    }

    // Add sanctions program
    public void addSanctionsProgram(SanctionsProgram program) {
        sanctionsPrograms.add(program);
        program.setSanctionedRecord(this);
    }

    // Add address
    public void addAddress(Address address) {
        addresses.add(address);
        address.setSanctionedRecord(this);
    }

    // Add identifier
    public void addIdentifier(Identifier identifier) {
        identifiers.add(identifier);
        identifier.setSanctionedRecord(this);
    }
}
