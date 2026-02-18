package tp_avancee_dev.tp_avancee.service;

import tp_avancee_dev.tp_avancee.model.User;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApiTokenService {

    private static final ApiTokenService INSTANCE = new ApiTokenService();

    private final Map<String, TokenSession> sessions = new ConcurrentHashMap<>();

    private ApiTokenService() {
    }

    public static ApiTokenService getInstance() {
        return INSTANCE;
    }

    public String createToken(User user) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new TokenSession(user.getId(), user.getUsername(), Instant.now()));
        return token;
    }

    public Optional<TokenSession> findSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(token));
    }

    public boolean isValid(String token) {
        return findSession(token).isPresent();
    }

    public static class TokenSession {
        private final Long userId;
        private final String username;
        private final Instant issuedAt;

        public TokenSession(Long userId, String username, Instant issuedAt) {
            this.userId = userId;
            this.username = username;
            this.issuedAt = issuedAt;
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
    }
    public void clear() {
        sessions.clear();
    }

}
