package com.kishuu.browserhistory.repository;

import com.kishuu.browserhistory.entity.HistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistoryEntryRepository extends JpaRepository<HistoryEntry, Long> {

    List<HistoryEntry> findByUserIdOrderBySequenceAsc(Long userId);

    @Query("SELECT MAX(h.sequence) FROM HistoryEntry h WHERE h.user.id = :userId")
    Optional<Long> findMaxSequenceForUser(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
