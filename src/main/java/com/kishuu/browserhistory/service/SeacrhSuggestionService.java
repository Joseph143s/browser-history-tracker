package com.kishuu.browserhistory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Calls Google's public autocomplete endpoint from the SERVER, not the
 * browser. Google's endpoint doesn't send CORS headers, so a direct
 * fetch() from frontend JS would be blocked -- but a server-to-server
 * HTTP call has no such restriction, since CORS only governs
 * browser-to-server requests.
 */
@Service
public class SearchSuggestionService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> getSuggestions(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            // client=firefox returns a simple ["query", ["s1","s2",...]] JSON array
            String url = "https://suggestqueries.google.com/complete/search?client=firefox&q=" + encoded;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(3))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }

            // Response shape: ["original query", ["suggestion1", "suggestion2", ...]]
            List<?> parsed = objectMapper.readValue(response.body(), List.class);
            if (parsed.size() < 2) return Collections.emptyList();

            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) parsed.get(1);
            return suggestions;

        } catch (Exception e) {
            // Network hiccup or Google endpoint unavailable -- degrade gracefully,
            // frontend falls back to its local known-sites list.
            return Collections.emptyList();
        }
    }
}
