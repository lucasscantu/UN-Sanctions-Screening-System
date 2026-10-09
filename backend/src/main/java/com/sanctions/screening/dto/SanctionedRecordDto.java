package com.sanctions.screening.dto;

import com.sanctions.screening.entity.RecordType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionedRecordDto {

    private Long id;
    private String unReferenceNumber;
    private RecordType recordType;
    private String primaryName;
    private String submittedName;
    private String sortKey;
    private String designation;
    private LocalDate listedOn;
    private String sourceUrl;
    private LocalDateTime firstImportedAt;
    private LocalDateTime lastSyncedAt;
    private boolean isActive;
    private String sourceStatus;
    private List<NameAliasDto> aliases;
}
