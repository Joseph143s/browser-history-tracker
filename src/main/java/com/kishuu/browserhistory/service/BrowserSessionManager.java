package com.kishuu.browserhistory.service;

import com.kishuu.browserhistory.entity.HistoryEntry;
import com.kishuu.browserhistory.repository.HistoryEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds one BrowserSession (DLL + back/forward stacks) per logged-in user,
 * keyed by userId. ConcurrentHashMap.computeIfAbsent is atomic, so if two
 * requests for the same brand-new user arrive at the same instant, only one
 * session object gets created and both requests share it -- while a
 * different user's session lives at a completely different map entry, so
 * concurrent users never block each other.
 *
 * This is in-memory only (lost on restart) by design -- it's a live
 * navigation cache. The source of truth is the history_entries table,
 * which is why we rebuild the DLL from DB on first access per user.
 */
@Component
@RequiredArgsConstructor
public class BrowserSessionManager {

    private final ConcurrentHashMap<Long, BrowserSession> sessions = new ConcurrentHashMap<>();
    private final HistoryEntryRepository historyEntryRepository;

    public BrowserSession getSession(Long userId) {
        return sessions.computeIfAbsent(userId, this::loadFromDatabase);
    }

    public void invalidate(Long userId) {
        sessions.remove(userId);
    }

    private BrowserSession loadFromDatabase(Long userId) {
        BrowserSession session = new BrowserSession();
        List<HistoryEntry> rows = historyEntryRepository.findByUserIdOrderBySequenceAsc(userId);

        // Rebuild the DLL tail-appending in stored order; this does NOT touch
        // the back/forward stacks -- those start fresh each server restart,
        // which mirrors how a real browser's back/forward resets on relaunch
        // while full history persists.
        for (HistoryEntry row : rows) {
            session.getHistory().addVisit(row.getId(), row.getUrl(), row.getTitle(), row.getVisitedAt());
        }
        session.primeCurrentToTail();
        return session;
    }
}
