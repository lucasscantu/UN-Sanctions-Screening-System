package com.sanctions.screening.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "name_aliases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NameAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sanctioned_record_id", nullable = false)
    private SanctionedRecord sanctionedRecord;

    @Column(name = "alias_name", nullable = false, length = 1000)
    private String aliasName;

    @Column(name = "alias_name_normalized", length = 1000)
    private String aliasNameNormalized;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality")
    private AliasQuality quality;

    @Column(name = "name_origin_desc", length = 500)
    private String nameOriginDesc;

    @Column(name = "is_primary_alias")
    private boolean isPrimaryAlias = false;
}
