package com.sanctions.screening.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisResult {

    private Long recordId;
    private String assessment; // HIGH, MEDIUM, LOW, INDETERMINATE
    private String explanation;
    private LinguisticVariantsDto linguisticVariants;
    private MatchingEvidenceDto matchingEvidence;
    private ConflictingEvidenceDto conflictingEvidence;
    private String warning;
    private String disclaimer;
    private String modelUsed;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LinguisticVariantsDto {
        private boolean transliterationVariants;
        private boolean spellingVariants;
        private boolean nameOrderDifferences;
        private boolean abbreviationDifferences;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MatchingEvidenceDto {
        private boolean exactMatch;
        private boolean partialMatch;
        private boolean aliasMatch;
        private String bestMatchingAlias;
        private List<String> matchingAliases;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConflictingEvidenceDto {
        private List<String> differentNames;
        private List<String> missingInformation;
    }
}
