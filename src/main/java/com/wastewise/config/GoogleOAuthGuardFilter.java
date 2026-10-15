package com.wastewise.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Blocks Spring Security's Google authorization redirect when no real client ID is configured,
 * so the browser never hits accounts.google.com with an empty/placeholder client.
 */
@Component
public class GoogleOAuthGuardFilter extends OncePerRequestFilter {

    private final OAuth2Config oauth2Config;

    public GoogleOAuthGuardFilter(OAuth2Config oauth2Config) {
        this.oauth2Config = oauth2Config;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/oauth2/authorization/google")
                && !oauth2Config.isGoogleConfigured()) {
            response.sendRedirect(request.getContextPath() + "/login?error=oauth");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
