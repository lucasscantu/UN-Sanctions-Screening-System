package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sanctions_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionsProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sanctioned_record_id", nullable = false)
    private SanctionedRecord sanctionedRecord;

    @Column(name = "committee", nullable = false, length = 100)
    private String committee;

    @Column(name = "program_name", length = 500)
    private String programName;

    @Column(name = "committee_url", length = 2000)
    private String committeeUrl;

    @Column(name = "listing_type", length = 200)
    private String listingType;

    @Column(name = "other_information", columnDefinition = "TEXT")
    private String otherInformation;
}
