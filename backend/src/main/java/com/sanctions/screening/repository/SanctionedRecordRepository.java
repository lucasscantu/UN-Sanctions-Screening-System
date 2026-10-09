package com.sanctions.screening.repository;

import com.sanctions.screening.entity.RecordType;
import com.sanctions.screening.entity.SanctionedRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanctionedRecordRepository extends JpaRepository<SanctionedRecord, Long>, JpaSpecificationExecutor<SanctionedRecord> {

    Optional<SanctionedRecord> findByUnReferenceNumber(String unReferenceNumber);

    List<SanctionedRecord> findByRecordType(RecordType recordType);

    long countByRecordType(RecordType recordType);

    long countByIsActive(boolean isActive);

    @Query("SELECT s FROM SanctionedRecord s WHERE s.isActive = true")
    Page<SanctionedRecord> findAllActive(Pageable pageable);

    @Query("SELECT s FROM SanctionedRecord s WHERE s.isActive = true AND " +
           "(LOWER(s.primaryNameNormalized) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.primaryName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<SanctionedRecord> searchByPrimaryName(String query, Pageable pageable);

    @Query("SELECT DISTINCT s FROM SanctionedRecord s " +
           "JOIN s.aliases a " +
           "WHERE s.isActive = true AND " +
           "(LOWER(s.primaryNameNormalized) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.primaryName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.aliasNameNormalized) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.aliasName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<SanctionedRecord> searchByNameOrAlias(String query, Pageable pageable);

    @Query("SELECT s FROM SanctionedRecord s WHERE s.unReferenceNumber IN :referenceNumbers")
    List<SanctionedRecord> findByUnReferenceNumbers(List<String> referenceNumbers);

    @Query("SELECT s FROM SanctionedRecord s WHERE s.isActive = true")
    List<SanctionedRecord> findAllActive();

    boolean existsByUnReferenceNumber(String unReferenceNumber);
}
