package com.sanctions.screening.repository;

import com.sanctions.screening.entity.ImportJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    List<ImportJob> findByStatusOrderByStartedAtDesc(String status);

    Optional<ImportJob> findByJobUuid(String jobUuid);

    List<ImportJob> findByStartedAtAfterOrderByStartedAtDesc(LocalDateTime since);

    List<ImportJob> findTop10ByOrderByStartedAtDesc();

    Page<ImportJob> findAllByOrderByStartedAtDesc(Pageable pageable);

    Optional<ImportJob> findTopByStatusOrderByStartedAtDesc(String status);

    long countByStatus(String status);
}
