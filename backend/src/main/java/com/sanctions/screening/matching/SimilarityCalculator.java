package com.sanctions.screening.matching;

import com.sanctions.screening.util.NameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SimilarityCalculator {

    private final NameNormalizer nameNormalizer;

    public double calculateSimilarity(String name1, String name2) {
        return calculateSimilarity(name1, name2, null, null);
    }

    public double calculateSimilarity(String name1, String name2, 
                                     List<String> aliases1, List<String> aliases2) {
        // Calculate multiple similarity metrics and combine them
        double levenshtein = calculateLevenshteinSimilarity(name1, name2);
        double jaroWinkler = calculateJaroWinklerSimilarity(name1, name2);
        double tokenBased = calculateTokenBasedSimilarity(name1, name2);
        double aliasBoost = calculateAliasBoost(name1, name2, aliases1, aliases2);

        // Weighted average
        double similarity = (levenshtein * 0.3) + 
                            (jaroWinkler * 0.35) + 
                            (tokenBased * 0.25) + 
                            (aliasBoost * 0.1);

        return Math.min(100, Math.max(0, similarity * 100));
    }

    public double calculateLevenshteinSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        String s1 = nameNormalizer.normalizeForComparison(str1);
        String s2 = nameNormalizer.normalizeForComparison(str2);
        
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        int distance = computeLevenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        
        if (maxLength == 0) {
            return 1.0;
        }
        
        return 1.0 - ((double) distance / maxLength);
    }

    public double calculateJaroWinklerSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        String s1 = nameNormalizer.normalizeForComparison(str1);
        String s2 = nameNormalizer.normalizeForComparison(str2);
        
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        return jaroWinklerSimilarity(s1, s2);
    }

    public double calculateTokenBasedSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        
        String s1 = nameNormalizer.normalizeForComparison(str1);
        String s2 = nameNormalizer.normalizeForComparison(str2);
        
        if (s1.isEmpty() && s2.isEmpty()) {
            return 1.0;
        }
        if (s1.isEmpty() || s2.isEmpty()) {
            return 0.0;
        }

        Set<String> tokens1 = new HashSet<>(Arrays.asList(s1.split("\\s+")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(s2.split("\\s+")));

        // Jaccard similarity
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);
        
        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }

    public double calculateAliasBoost(String name1, String name2, 
                                     List<String> aliases1, List<String> aliases2) {
        if ((aliases1 == null || aliases1.isEmpty()) && 
            (aliases2 == null || aliases2.isEmpty())) {
            return 0.0;
        }

        double boost = 0.0;
        int aliasCount = 0;

        // Check if name1 matches any alias of name2
        if (aliases2 != null) {
            for (String alias : aliases2) {
                double similarity = calculateTokenBasedSimilarity(name1, alias);
                if (similarity > 0.8) {
                    boost += similarity * 0.2;
                    aliasCount++;
                }
            }
        }

        // Check if name2 matches any alias of name1
        if (aliases1 != null) {
            for (String alias : aliases1) {
                double similarity = calculateTokenBasedSimilarity(name2, alias);
                if (similarity > 0.8) {
                    boost += similarity * 0.2;
                    aliasCount++;
                }
            }
        }

        // Normalize boost
        if (aliasCount > 0) {
            boost = boost / aliasCount;
        }

        return Math.min(1.0, boost);
    }

    private int computeLevenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                int cost = (str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,      // deletion
                        dp[i][j - 1] + 1),     // insertion
                        dp[i - 1][j - 1] + cost); // substitution
            }
        }

        return dp[str1.length()][str2.length()];
    }

    private double jaroWinklerSimilarity(String s1, String s2) {
        double jaro = jaroSimilarity(s1, s2);
        
        // Jaro-Winkler modification
        int prefixLength = Math.min(Math.min(s1.length(), s2.length()), 4);
        int matchingPrefix = 0;
        
        for (int i = 0; i < prefixLength; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                matchingPrefix++;
            } else {
                break;
            }
        }
        
        double winkler = jaro + (0.1 * matchingPrefix * (1.0 - jaro));
        
        return Math.min(1.0, winkler);
    }

    private double jaroSimilarity(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        if (len1 == 0 && len2 == 0) {
            return 1.0;
        }
        if (len1 == 0 || len2 == 0) {
            return 0.0;
        }

        int matchDistance = Math.max(len1, len2) / 2 - 1;
        
        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];
        
        int matches = 0;
        
        for (int i = 0; i < len1; i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);
            
            for (int j = start; j < end; j++) {
                if (!s2Matches[j] && s1.charAt(i) == s2.charAt(j)) {
                    s1Matches[i] = true;
                    s2Matches[j] = true;
                    matches++;
                    break;
                }
            }
        }
        
        if (matches == 0) {
            return 0.0;
        }

        int transpositions = 0;
        int k = 0;
        
        for (int i = 0; i < len1; i++) {
            if (s1Matches[i]) {
                while (!s2Matches[k]) {
                    k++;
                }
                if (s1.charAt(i) != s2.charAt(k)) {
                    transpositions++;
                }
                k++;
            }
        }

        double jaro = ((double) matches / len1 + 
                      (double) matches / len2 + 
                      (double) (matches - transpositions / 2.0) / matches) / 3.0;
        
        return jaro;
    }

    public SimilarityResult calculateDetailedSimilarity(String searchName, SanctionedRecordCandidate candidate) {
        String primaryName = candidate.getPrimaryName();
        List<String> aliases = candidate.getAliases();

        double levenshtein = calculateLevenshteinSimilarity(searchName, primaryName);
        double jaroWinkler = calculateJaroWinklerSimilarity(searchName, primaryName);
        double tokenBased = calculateTokenBasedSimilarity(searchName, primaryName);
        double aliasBoost = calculateAliasBoost(searchName, primaryName, null, aliases);

        // Find best alias match
        double bestAliasMatch = 0.0;
        String bestAlias = null;
        
        if (aliases != null) {
            for (String alias : aliases) {
                double aliasSimilarity = calculateTokenBasedSimilarity(searchName, alias);
                if (aliasSimilarity > bestAliasMatch) {
                    bestAliasMatch = aliasSimilarity;
                    bestAlias = alias;
                }
            }
        }

        double combined = (levenshtein * 0.3) + 
                          (jaroWinkler * 0.35) + 
                          (tokenBased * 0.25) + 
                          (aliasBoost * 0.1);

        double finalScore = Math.min(100, Math.max(0, combined * 100));

        return SimilarityResult.builder()
                .searchName(searchName)
                .candidateName(primaryName)
                .levenshteinSimilarity(levenshtein * 100)
                .jaroWinklerSimilarity(jaroWinkler * 100)
                .tokenBasedSimilarity(tokenBased * 100)
                .aliasSimilarity(bestAliasMatch * 100)
                .bestMatchingAlias(bestAlias)
                .combinedScore(finalScore)
                .build();
    }

    public record SimilarityResult(
            String searchName,
            String candidateName,
            double levenshteinSimilarity,
            double jaroWinklerSimilarity,
            double tokenBasedSimilarity,
            double aliasSimilarity,
            String bestMatchingAlias,
            double combinedScore
    ) {
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String searchName;
            private String candidateName;
            private double levenshteinSimilarity;
            private double jaroWinklerSimilarity;
            private double tokenBasedSimilarity;
            private double aliasSimilarity;
            private String bestMatchingAlias;
            private double combinedScore;

            public Builder searchName(String searchName) {
                this.searchName = searchName;
                return this;
            }

            public Builder candidateName(String candidateName) {
                this.candidateName = candidateName;
                return this;
            }

            public Builder levenshteinSimilarity(double levenshteinSimilarity) {
                this.levenshteinSimilarity = levenshteinSimilarity;
                return this;
            }

            public Builder jaroWinklerSimilarity(double jaroWinklerSimilarity) {
                this.jaroWinklerSimilarity = jaroWinklerSimilarity;
                return this;
            }

            public Builder tokenBasedSimilarity(double tokenBasedSimilarity) {
                this.tokenBasedSimilarity = tokenBasedSimilarity;
                return this;
            }

            public Builder aliasSimilarity(double aliasSimilarity) {
                this.aliasSimilarity = aliasSimilarity;
                return this;
            }

            public Builder bestMatchingAlias(String bestMatchingAlias) {
                this.bestMatchingAlias = bestMatchingAlias;
                return this;
            }

            public Builder combinedScore(double combinedScore) {
                this.combinedScore = combinedScore;
                return this;
            }

            public SimilarityResult build() {
                return new SimilarityResult(
                        searchName, candidateName, levenshteinSimilarity, jaroWinklerSimilarity,
                        tokenBasedSimilarity, aliasSimilarity, bestMatchingAlias, combinedScore
                );
            }
        }
    }
}
