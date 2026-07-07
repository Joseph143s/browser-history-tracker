package com.kishuu.browserhistory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** Sent when the user (or extension) navigates to a new URL */
@Getter
@Setter
public class HistoryVisitRequest {

    @NotBlank(message = "URL is required")
    private String url;

    private String title;
}
