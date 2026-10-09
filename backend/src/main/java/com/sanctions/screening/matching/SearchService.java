package com.sanctions.screening.matching;

import com.sanctions.screening.config.SanctionsConfig;
import com.sanctions.screening.dto.SearchRequest;
import com.sanctions.screening.dto.SearchResult;
import com.sanctions.screening.entity.NameAlias;
import com.sanctions.screening.entity.SanctionedRecord;
import com.sanctions.screening.repository.NameAliasRepository;
import com.sanctions.screening.repository.SanctionedRecordRepository;
import com.sanctions.screening.util.NameNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final SanctionedRecordRepository sanctionedRecordRepository;
    private final NameAliasRepository nameAliasRepository;
    private final SimilarityCalculator similarityCalculator;
    private final NameNormalizer nameNormalizer;
    private final SanctionsConfig.SearchProperties searchProperties;

    public SearchResult search(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        String query = request.getName();
        if (query == null || query.trim().isEmpty()) {
            return SearchResult.builder()
                    .query("")
                    .totalResults(0)
                    .results(Collections.emptyList())
                    .searchDurationMs(0)
                    .build();
        }

        query = query.trim();
        String normalizedQuery = nameNormalizer.normalize(query);

        // Step 1: Retrieve candidates using database search
        List<SanctionedRecordCandidate> candidates = retrieveCandidates(query, request);

        // Step 2: Calculate similarity scores
        List<SearchResult.ResultItem> results = calculateScores(candidates, query, request);

        // Step 3: Apply filters and sorting
        results = filterAndSort(results, request);

        long searchDuration = System.currentTimeMillis() - startTime;

        return SearchResult.builder()
                .query(query)
                .normalizedQuery(normalizedQuery)
                .totalResults(results.size())
                .results(results)
                .searchDurationMs(searchDuration)
                .candidatesRetrieved(candidates.size())
                .build();
    }

    private List<SanctionedRecordCandidate> retrieveCandidates(String query, SearchRequest request) {
        int limit = searchProperties.candidateLimit();
        
        // First, try to find exact matches
        List<SanctionedRecord> exactMatches = sanctionedRecordRepository
                .searchByNameOrAlias(query, PageRequest.of(0, limit));
        
        List<SanctionedRecordCandidate> candidates = exactMatches.stream()
                .map(this::convertToCandidate)
                .collect(Collectors.toList());

        // If we don't have enough candidates, expand the search
        if (candidates.size() < limit) {
            // Use normalized query for broader matching
            String normalizedQuery = nameNormalizer.normalize(query);
            
            // Find records where normalized name contains query tokens
            List<SanctionedRecord> additionalMatches = sanctionedRecordRepository
                    .findAllActive(PageRequest.of(0, limit * 2)).getContent();
            
            for (SanctionedRecord record : additionalMatches) {
                SanctionedRecordCandidate candidate = convertToCandidate(record);
                if (!candidates.contains(candidate)) {
                    candidates.add(candidate);
                    if (candidates.size() >= limit) {
                        break;
                    }
                }
            }
        }

        return candidates;
    }

    private SanctionedRecordCandidate convertToCandidate(SanctionedRecord record) {
        List<NameAlias> aliases = nameAliasRepository.findBySanctionedRecordId(record.getId());
        
        List<String> aliasNames = aliases.stream()
                .map(NameAlias::getAliasName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        List<String> aliasNamesNormalized = aliases.stream()
                .map(a -> a.getAliasNameNormalized() != null ? a.getAliasNameNormalized() : 
                     nameNormalizer.normalize(a.getAliasName()))
                .collect(Collectors.toList());

        return SanctionedRecordCandidate.builder()
                .id(record.getId())
                .unReferenceNumber(record.getUnReferenceNumber())
                .recordType(record.getRecordType())
                .primaryName(record.getPrimaryName())
                .primaryNameNormalized(record.getPrimaryNameNormalized())
                .aliases(aliasNames)
                .aliasesNormalized(aliasNamesNormalized)
                .aliasCount(aliases.size())
                .build();
    }

    private List<SearchResult.ResultItem> calculateScores(
            List<SanctionedRecordCandidate> candidates, 
            String query, 
            SearchRequest request) {
        
        List<SearchResult.ResultItem> results = new ArrayList<>();
        
        for (SanctionedRecordCandidate candidate : candidates) {
            SimilarityCalculator.SimilarityResult similarityResult = 
                    similarityCalculator.calculateDetailedSimilarity(query, candidate);
            
            double score = similarityResult.combinedScore();
            
            SearchResult.ResultItem resultItem = SearchResult.ResultItem.builder()
                    .recordId(candidate.getId())
                    .unReferenceNumber(candidate.getUnReferenceNumber())
                    .recordType(candidate.getRecordType())
                    .primaryName(candidate.getPrimaryName())
                    .matchingAlias(similarityResult.bestMatchingAlias())
                    .similarityScore(score)
                    .levenshteinSimilarity(similarityResult.levenshteinSimilarity())
                    .jaroWinklerSimilarity(similarityResult.jaroWinklerSimilarity())
                    .tokenBasedSimilarity(similarityResult.tokenBasedSimilarity())
                    .aliasSimilarity(similarityResult.aliasSimilarity())
                    .aliases(candidate.getAliases())
                    .aliasCount(candidate.getAliasCount())
                    .build();
            
            results.add(resultItem);
        }
        
        return results;
    }

    private List<SearchResult.ResultItem> filterAndSort(
            List<SearchResult.ResultItem> results, 
            SearchRequest request) {
        
        // Apply minimum score filter
        double minScore = request.getMinScore() != null ? request.getMinScore() : searchProperties.minScoreFilter();
        
        List<SearchResult.ResultItem> filtered = results.stream()
                .filter(r -> r.getSimilarityScore() >= minScore)
                .collect(Collectors.toList());

        // Sort by similarity score (descending)
        filtered.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

        return filtered;
    }

    public List<SanctionedRecord> findSimilarRecords(String name, double minSimilarity) {
        List<SanctionedRecord> allRecords = sanctionedRecordRepository.findAllActive();
        List<SanctionedRecord> similar = new ArrayList<>();

        for (SanctionedRecord record : allRecords) {
            double similarity = similarityCalculator.calculateSimilarity(name, record.getPrimaryName());
            if (similarity >= minSimilarity) {
                similar.add(record);
            }
        }

        return similar;
    }
}
