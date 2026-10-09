package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "biographical_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiographicalDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sanctioned_record_id", nullable = false)
    private SanctionedRecord sanctionedRecord;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_precision")
    private DatePrecision datePrecision;

    @Column(name = "place_of_birth", length = 1000)
    private String placeOfBirth;

    @Column(name = "city_of_birth", length = 500)
    private String cityOfBirth;

    @Column(name = "country_of_birth", length = 200)
    private String countryOfBirth;

    @Column(name = "nationality", length = 500)
    private String nationality;

    @Column(name = "gender", length = 50)
    private String gender;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "position", length = 500)
    private String position;

    @Column(name = "function", length = 500)
    private String function;
}
