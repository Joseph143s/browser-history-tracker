package com.kishuu.browserhistory.service;

import com.kishuu.browserhistory.entity.HistoryEntry;
import com.kishuu.browserhistory.entity.User;
import com.kishuu.browserhistory.exception.ApiException;
import com.kishuu.browserhistory.repository.HistoryEntryRepository;
import com.kishuu.browserhistory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final BrowserSessionManager sessionManager;
    private final HistoryEntryRepository historyEntryRepository;
    private final UserRepository userRepository;

    /** Record a new visit: persist to DB first (source of truth), then mirror into the in-memory DLL + stacks. */
    @Transactional
    public HistoryResponse recordVisit(Long userId, HistoryVisitRequest request) {
        User user = getUser(userId);

        long nextSequence = historyEntryRepository.findMaxSequenceForUser(userId).orElse(0L) + 1;
        LocalDateTime now = LocalDateTime.now();

        HistoryEntry entry = HistoryEntry.builder()
                .url(request.getUrl())
                .title(request.getTitle())
                .visitedAt(now)
                .sequence(nextSequence)
                .user(user)
                .build();
        entry = historyEntryRepository.save(entry);

        BrowserSession session = sessionManager.getSession(userId);
        HistoryNode node = session.visit(entry.getId(), entry.getUrl(), entry.getTitle(), entry.getVisitedAt());

        return toResponse(node, session);
    }

    public HistoryResponse goBack(Long userId) {
        BrowserSession session = sessionManager.getSession(userId);
        HistoryNode node = session.goBack();
        if (node == null) {
            throw new ApiException("No earlier page to go back to", HttpStatus.BAD_REQUEST);
        }
        return toResponse(node, session);
    }

    public HistoryResponse goForward(Long userId) {
        BrowserSession session = sessionManager.getSession(userId);
        HistoryNode node = session.goForward();
        if (node == null) {
            throw new ApiException("No later page to go forward to", HttpStatus.BAD_REQUEST);
        }
        return toResponse(node, session);
    }

    /** Full persisted timeline, newest first, with the current pointer flagged. */
    public List<HistoryResponse> getFullHistory(Long userId) {
        BrowserSession session = sessionManager.getSession(userId);
        HistoryNode current = session.getCurrent();

        return session.getHistory().toListNewestFirst().stream()
                .map(node -> HistoryResponse.builder()
                        .id(node.entryId)
                        .url(node.url)
                        .title(node.title)
                        .visitedAt(node.visitedAt)
                        .isCurrent(current != null && current.entryId != null && current.entryId.equals(node.entryId))
                        .build())
                .toList();
    }

    @Transactional
    public void deleteEntry(Long userId, Long entryId) {
        HistoryEntry entry = historyEntryRepository.findById(entryId)
                .orElseThrow(() -> new ApiException("History entry not found", HttpStatus.NOT_FOUND));

        if (!entry.getUser().getId().equals(userId)) {
            throw new ApiException("Not allowed to delete another user's history", HttpStatus.FORBIDDEN);
        }

        historyEntryRepository.delete(entry);

        // Invalidate the in-memory session so it's rebuilt clean from the DB
        // on next access -- simplest way to keep DLL + stacks consistent
        // after a random-access delete (as opposed to only ever removing the tail).
        sessionManager.invalidate(userId);
    }

    @Transactional
    public void clearAll(Long userId) {
        historyEntryRepository.deleteByUserId(userId);
        sessionManager.invalidate(userId);
    }

    private HistoryResponse toResponse(HistoryNode node, BrowserSession session) {
        return HistoryResponse.builder()
                .id(node.entryId)
                .url(node.url)
                .title(node.title)
                .visitedAt(node.visitedAt)
                .isCurrent(true)
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }
}
