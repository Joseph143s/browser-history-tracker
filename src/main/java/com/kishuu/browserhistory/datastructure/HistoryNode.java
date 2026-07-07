package com.kishuu.browserhistory.datastructure;

import java.time.LocalDateTime;

/**
 * A node in the browser history doubly linked list.
 * prev -> older page, next -> newer page (chronological order).
 */
public class HistoryNode {

    public Long entryId;      // maps back to HistoryEntry.id in DB (null until persisted)
    public String url;
    public String title;
    public LocalDateTime visitedAt;

    public HistoryNode prev;
    public HistoryNode next;

    public HistoryNode(Long entryId, String url, String title, LocalDateTime visitedAt) {
        this.entryId = entryId;
        this.url = url;
        this.title = title;
        this.visitedAt = visitedAt;
    }
}
