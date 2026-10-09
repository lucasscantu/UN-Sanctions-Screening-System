package com.sanctions.screening.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportStatusDto {

    private boolean importInProgress;
    private ImportJobDto lastImportJob;
    private long totalRecords;
    private long totalIndividuals;
    private long totalEntities;
}
