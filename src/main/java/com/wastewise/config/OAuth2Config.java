package com.wastewise.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Detects whether real Google OAuth2 credentials are present.
 * Spring Boot still auto-registers Google if client-id is an empty string,
 * which produces Google Error 401: invalid_client — so callers must check this first.
 */
@Configuration
public class OAuth2Config {

    private static final Logger log = LoggerFactory.getLogger(OAuth2Config.class);

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String clientSecret;

    public boolean isGoogleConfigured() {
        return isValidClientId(clientId) && isValidSecret(clientSecret);
    }

    /**
     * Strip accidental spaces/newlines (common when pasting into Render) so Google
     * receives a client ID that actually exists.
     */
    @Bean
    @ConditionalOnProperty(name = "spring.security.oauth2.client.registration.google.client-id")
    public ClientRegistrationRepository googleClientRegistrationRepository() {
        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(cleanClientId(clientId))
                .clientSecret(cleanSecret(clientSecret))
                .scope("openid", "profile", "email")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }

    @PostConstruct
    void logGoogleOAuthStatus() {
        if (isGoogleConfigured()) {
            String cleaned = cleanClientId(clientId);
            log.info("Google Sign-In is configured (client id ...{})",
                    cleaned.substring(Math.max(0, cleaned.length() - 12)));
        } else {
            log.warn("Google Sign-In is NOT configured. Set GOOGLE_CLIENT_ID and "
                    + "GOOGLE_CLIENT_SECRET in application-local.properties (or env vars). "
                    + "Authorized redirect URI: http://localhost:8080/login/oauth2/code/google");
        }
    }

    private static boolean isValidClientId(String value) {
        String id = cleanClientId(value);
        if (!hasText(id) || isPlaceholder(id)) {
            return false;
        }
        return id.endsWith(".apps.googleusercontent.com");
    }

    private static boolean isValidSecret(String value) {
        String secret = cleanSecret(value);
        return hasText(secret) && !isPlaceholder(secret);
    }

    static String cleanClientId(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    static String cleanSecret(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean isPlaceholder(String value) {
        String lower = value.toLowerCase();
        return lower.contains("your_")
                || lower.contains("your-")
                || lower.contains("replace")
                || lower.contains("placeholder")
                || lower.contains("changeme")
                || "disabled".equals(lower);
    }
}
