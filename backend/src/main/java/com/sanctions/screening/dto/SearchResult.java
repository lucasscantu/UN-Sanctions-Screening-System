package com.sanctions.screening.dto;

import com.sanctions.screening.entity.RecordType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResult {

    private String query;
    private String normalizedQuery;
    private int totalResults;
    private List<ResultItem> results;
    private long searchDurationMs;
    private int candidatesRetrieved;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultItem {
        private Long recordId;
        private String unReferenceNumber;
        private RecordType recordType;
        private String primaryName;
        private String matchingAlias;
        private double similarityScore;
        private double levenshteinSimilarity;
        private double jaroWinklerSimilarity;
        private double tokenBasedSimilarity;
        private double aliasSimilarity;
        private List<String> aliases;
        private int aliasCount;
    }
}
