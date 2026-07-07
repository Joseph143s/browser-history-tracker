package com.kishuu.browserhistory.security;

import com.kishuu.browserhistory.entity.User;
import com.kishuu.browserhistory.exception.ApiException;
import com.kishuu.browserhistory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Small helper so controllers never touch SecurityContextHolder directly.
 * Because Spring Security's context is ThreadLocal-scoped per request,
 * this always resolves to whichever user's JWT is attached to the
 * CURRENT request thread -- safe under concurrent multi-user traffic.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException("Not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.UNAUTHORIZED));
        return user.getId();
    }
}
