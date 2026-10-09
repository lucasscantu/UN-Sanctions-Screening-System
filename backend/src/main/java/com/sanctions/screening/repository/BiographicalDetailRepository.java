package com.sanctions.screening.repository;

import com.sanctions.screening.entity.BiographicalDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BiographicalDetailRepository extends JpaRepository<BiographicalDetail, Long> {

    List<BiographicalDetail> findBySanctionedRecordId(Long recordId);

    @Modifying
    @Query("DELETE FROM BiographicalDetail b WHERE b.sanctionedRecord.id = :recordId")
    void deleteBySanctionedRecordId(Long recordId);
}
