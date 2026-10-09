package com.sanctions.screening.repository;

import com.sanctions.screening.entity.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Page<SearchHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<SearchHistory> findTop10ByOrderByCreatedAtDesc();

    List<SearchHistory> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);

    Page<SearchHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByUserId(String userId);

    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
