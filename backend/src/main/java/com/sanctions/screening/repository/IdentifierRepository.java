package com.sanctions.screening.repository;

import com.sanctions.screening.entity.Identifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentifierRepository extends JpaRepository<Identifier, Long> {

    List<Identifier> findBySanctionedRecordId(Long recordId);

    @Modifying
    @Query("DELETE FROM Identifier i WHERE i.sanctionedRecord.id = :recordId")
    void deleteBySanctionedRecordId(Long recordId);
}
