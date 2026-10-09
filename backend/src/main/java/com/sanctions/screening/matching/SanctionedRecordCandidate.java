package com.sanctions.screening.matching;

import com.sanctions.screening.entity.RecordType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionedRecordCandidate {
    
    private Long id;
    private String unReferenceNumber;
    private RecordType recordType;
    private String primaryName;
    private String primaryNameNormalized;
    private List<String> aliases;
    private List<String> aliasesNormalized;
    
    // Additional fields for ranking
    private double similarityScore;
    private String matchingAlias;
    private int aliasCount;
}
