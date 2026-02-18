package tp_avancee_dev.tp_avancee;

import org.junit.jupiter.api.Test;
import tp_avancee_dev.tp_avancee.api.security.ApiTokenServiceCallback;
import tp_avancee_dev.tp_avancee.api.security.BearerTokenLoginModule;
import tp_avancee_dev.tp_avancee.api.security.RolePrincipal;
import tp_avancee_dev.tp_avancee.api.security.TokenCallback;
import tp_avancee_dev.tp_avancee.api.security.UserPrincipal;
import tp_avancee_dev.tp_avancee.service.ApiTokenService;
import org.junit.jupiter.api.BeforeEach;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BearerTokenLoginModuleTest {

    @BeforeEach
    void clearTokenStore() {
        ApiTokenService.getInstance().clear();
    }

    @Test
    void login_validToken_shouldPopulateSubjectWithIdentityAndRoles() throws Exception {
        ApiTokenService tokenService = ApiTokenService.getInstance();
        String token = tokenService.createToken(42L, "admin");

        Subject subject = new Subject();
        BearerTokenLoginModule module = new BearerTokenLoginModule();
        module.initialize(subject, new ModuleCallbackHandler(token, tokenService), Map.of(), Map.of());

        assertTrue(module.login());
        assertTrue(module.commit());

        UserPrincipal userPrincipal = subject.getPrincipals(UserPrincipal.class).stream().findFirst().orElseThrow();
        assertEquals(42L, userPrincipal.getUserId());
        assertEquals("admin", userPrincipal.getName());
        assertTrue(subject.getPrincipals(RolePrincipal.class).stream().anyMatch(r -> "ROLE_USER".equals(r.getName())));
        assertTrue(subject.getPrincipals(RolePrincipal.class).stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())));
    }

    @Test
    void login_invalidToken_shouldFail() {
        ApiTokenService tokenService = ApiTokenService.getInstance();

        Subject subject = new Subject();
        BearerTokenLoginModule module = new BearerTokenLoginModule();
        module.initialize(subject, new ModuleCallbackHandler("bad-token", tokenService), Map.of(), Map.of());

        assertThrows(FailedLoginException.class, module::login);
    }

    @Test
    void login_expiredToken_shouldFail() throws Exception {
        ApiTokenService tokenService = ApiTokenService.getInstance();
        String expiredToken = "expired-token-" + System.nanoTime();

        getSessions(tokenService).put(
                expiredToken,
                new ApiTokenService.TokenSession(5L, "alice", Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600))
        );

        Subject subject = new Subject();
        BearerTokenLoginModule module = new BearerTokenLoginModule();
        module.initialize(subject, new ModuleCallbackHandler(expiredToken, tokenService), Map.of(), Map.of());

        assertThrows(FailedLoginException.class, module::login);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ApiTokenService.TokenSession> getSessions(ApiTokenService tokenService) throws Exception {
        Field sessionsField = ApiTokenService.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        return (Map<String, ApiTokenService.TokenSession>) sessionsField.get(tokenService);
    }

    private static class ModuleCallbackHandler implements CallbackHandler {
        private final String token;
        private final ApiTokenService tokenService;

        private ModuleCallbackHandler(String token, ApiTokenService tokenService) {
            this.token = token;
            this.tokenService = tokenService;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof TokenCallback) {
                    ((TokenCallback) callback).setToken(token);
                } else if (callback instanceof ApiTokenServiceCallback) {
                    ((ApiTokenServiceCallback) callback).setApiTokenService(tokenService);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }
    }
}
