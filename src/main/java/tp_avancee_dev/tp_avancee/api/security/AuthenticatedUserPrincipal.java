package tp_avancee_dev.tp_avancee.api.security;

import java.security.Principal;

public class AuthenticatedUserPrincipal implements Principal {

    private final Long userId;
    private final String username;

    public AuthenticatedUserPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return username;
    }
}
