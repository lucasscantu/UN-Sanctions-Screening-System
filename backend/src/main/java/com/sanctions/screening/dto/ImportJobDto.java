package com.sanctions.screening.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportJobDto {

    private Long id;
    private String jobUuid;
    private String sourceUrl;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int recordsCreated;
    private int recordsUpdated;
    private int recordsDeactivated;
    private int recordsFailed;
    private int totalRecordsProcessed;
    private String errorSummary;
    private long fileSizeBytes;
    private boolean isManualTrigger;
    private String triggeredBy;
}
