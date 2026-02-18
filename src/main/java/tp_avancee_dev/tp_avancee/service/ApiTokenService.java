package tp_avancee_dev.tp_avancee.service;

import tp_avancee_dev.tp_avancee.model.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApiTokenService {

    public static final long TOKEN_TTL_SECONDS = 3600L;

    private static final ApiTokenService INSTANCE = new ApiTokenService();

    private final Map<String, TokenSession> sessions = new ConcurrentHashMap<>();

    private ApiTokenService() {
    }

    public static ApiTokenService getInstance() {
        return INSTANCE;
    }

    public String createToken(User user) {
        return createToken(user.getId(), user.getUsername());
    }

    public String createToken(Long userId, String username) {
        String token = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(TOKEN_TTL_SECONDS, ChronoUnit.SECONDS);
        sessions.put(token, new TokenSession(userId, username, issuedAt, expiresAt));
        return token;
    }

    public Optional<TokenSession> findSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }

        if (session.isExpired()) {
            sessions.remove(token);
            return Optional.empty();
        }

        return Optional.of(session);
    }

    public boolean isValid(String token) {
        return findSession(token).isPresent();
    }

    public static class TokenSession {
        private final Long userId;
        private final String username;
        private final Instant issuedAt;
        private final Instant expiresAt;

        public TokenSession(Long userId, String username, Instant issuedAt, Instant expiresAt) {
            this.userId = userId;
            this.username = username;
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public Instant getIssuedAt() {
            return issuedAt;
        }

        public Instant getExpiresAt() {
            return expiresAt;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
    public void clear() {
        sessions.clear();
    }

}
