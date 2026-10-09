package com.sanctions.screening.repository;

import com.sanctions.screening.entity.NameAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NameAliasRepository extends JpaRepository<NameAlias, Long> {

    List<NameAlias> findBySanctionedRecordId(Long recordId);

    @Query("SELECT a FROM NameAlias a WHERE a.sanctionedRecord.isActive = true AND " +
           "(LOWER(a.aliasNameNormalized) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.aliasName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<NameAlias> findByAliasNameContainingIgnoreCase(String query);

    long countBySanctionedRecordId(Long recordId);
}
