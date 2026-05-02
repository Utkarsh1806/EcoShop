package com.ecoshop.fraud.service.repo;

import com.ecoshop.fraud.service.domain.BlocklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlocklistEntryRepository extends JpaRepository<BlocklistEntry, UUID> {
    Optional<BlocklistEntry> findByEntryTypeAndEntryValue(String entryType, String entryValue);
}
