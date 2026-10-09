package com.sanctions.screening.repository;

import com.sanctions.screening.entity.SanctionsProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanctionsProgramRepository extends JpaRepository<SanctionsProgram, Long> {

    List<SanctionsProgram> findBySanctionedRecordId(Long recordId);

    @Modifying
    @Query("DELETE FROM SanctionsProgram s WHERE s.sanctionedRecord.id = :recordId")
    void deleteBySanctionedRecordId(Long recordId);
}
