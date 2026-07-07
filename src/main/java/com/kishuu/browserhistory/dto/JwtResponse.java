package com.kishuu.browserhistory.dto;

import lombok.Getter;

@Getter
public class JwtResponse {
    private final String token;
    private final String username;
    private final Long userId;
    private final String tokenType = "Bearer";

    public JwtResponse(String token, String username, Long userId) {
        this.token = token;
        this.username = username;
        this.userId = userId;
    }
}
