package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "identifiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Identifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sanctioned_record_id", nullable = false)
    private SanctionedRecord sanctionedRecord;

    @Column(name = "identifier_type", length = 100)
    private String identifierType; // PASSPORT, NATIONAL_ID, etc.

    @Column(name = "identifier_number", length = 200)
    private String identifierNumber;

    @Column(name = "issuing_country", length = 200)
    private String issuingCountry;

    @Column(name = "issuing_authority", length = 500)
    private String issuingAuthority;

    @Column(name = "expiration_date", length = 50)
    private String expirationDate;

    @Column(name = "notes", length = 1000)
    private String notes;
}
