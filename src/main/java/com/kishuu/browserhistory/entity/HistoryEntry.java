package com.kishuu.browserhistory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A single visited URL. Rows are stored flat (no prev/next FK) because
 * ordering is derived from visitedAt + sequence when we rebuild the
 * in-memory DoublyLinkedList for a user. Keeping persistence flat avoids
 * having to patch neighbouring rows' prev/next pointers on every insert/delete.
 */
@Entity
@Table(name = "history_entries", indexes = {
        @Index(name = "idx_user_sequence", columnList = "user_id, sequence")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 512)
    private String title;

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    /** Monotonically increasing per-user counter -> gives DLL insertion order */
    @Column(nullable = false)
    private Long sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
