package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sanctioned_record_id", nullable = false)
    private SanctionedRecord sanctionedRecord;

    @Column(name = "street", length = 1000)
    private String street;

    @Column(name = "city", length = 500)
    private String city;

    @Column(name = "state_or_province", length = 500)
    private String stateOrProvince;

    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Column(name = "country", length = 200)
    private String country;

    @Column(name = "address_type", length = 100)
    private String addressType;

    @Column(name = "is_primary")
    private boolean isPrimary = false;
}
