package com.sanctions.screening.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NameAliasDto {

    private Long id;
    private String aliasName;
    private String quality;
    private String nameOriginDesc;
}
