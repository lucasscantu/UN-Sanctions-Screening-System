package com.sanctions.screening.repository;

import com.sanctions.screening.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findBySanctionedRecordId(Long recordId);

    @Modifying
    @Query("DELETE FROM Address a WHERE a.sanctionedRecord.id = :recordId")
    void deleteBySanctionedRecordId(Long recordId);
}
