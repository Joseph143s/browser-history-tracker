package com.kishuu.browserhistory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class HistoryResponse {
    private Long id;
    private String url;
    private String title;
    private LocalDateTime visitedAt;
    private boolean isCurrent; // marks the node the back/forward pointer is on
}
