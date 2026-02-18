package tp_avancee_dev.tp_avancee;

import org.junit.jupiter.api.Test;
import tp_avancee_dev.tp_avancee.api.security.AuthServiceCallback;
import tp_avancee_dev.tp_avancee.api.security.DbLoginModule;
import tp_avancee_dev.tp_avancee.api.security.RolePrincipal;
import tp_avancee_dev.tp_avancee.api.security.UserPrincipal;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.service.AuthService;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DbLoginModuleTest {

    @Test
    void login_validCredentials_shouldPopulateSubjectPrincipals() throws Exception {
        AuthService authService = mock(AuthService.class);
        User user = new User();
        user.setId(10L);
        user.setUsername("alice");
        when(authService.login("alice", "secret")).thenReturn(user);

        Subject subject = new Subject();
        DbLoginModule module = new DbLoginModule();
        module.initialize(subject, new ModuleCallbackHandler("alice", "secret", authService), Map.of(), Map.of());

        assertTrue(module.login());
        assertTrue(module.commit());

        UserPrincipal principal = subject.getPrincipals(UserPrincipal.class).stream().findFirst().orElseThrow();
        assertEquals(10L, principal.getUserId());
        assertEquals("alice", principal.getName());
        assertTrue(subject.getPrincipals(RolePrincipal.class).stream().anyMatch(r -> "ROLE_USER".equals(r.getName())));
    }

    @Test
    void login_adminCredentials_shouldGrantAdminRole() throws Exception {
        AuthService authService = mock(AuthService.class);
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        when(authService.login("admin", "secret")).thenReturn(user);

        Subject subject = new Subject();
        DbLoginModule module = new DbLoginModule();
        module.initialize(subject, new ModuleCallbackHandler("admin", "secret", authService), Map.of(), Map.of());

        assertTrue(module.login());
        assertTrue(module.commit());

        assertTrue(subject.getPrincipals(RolePrincipal.class).stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName())));
    }

    @Test
    void login_invalidCredentials_shouldFail() {
        AuthService authService = mock(AuthService.class);
        when(authService.login("alice", "wrong")).thenReturn(null);

        Subject subject = new Subject();
        DbLoginModule module = new DbLoginModule();
        module.initialize(subject, new ModuleCallbackHandler("alice", "wrong", authService), Map.of(), Map.of());

        assertThrows(FailedLoginException.class, module::login);
    }

    private static class ModuleCallbackHandler implements CallbackHandler {
        private final String username;
        private final String password;
        private final AuthService authService;

        private ModuleCallbackHandler(String username, String password, AuthService authService) {
            this.username = username;
            this.password = password;
            this.authService = authService;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(username);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password.toCharArray());
                } else if (callback instanceof AuthServiceCallback) {
                    ((AuthServiceCallback) callback).setAuthService(authService);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }
    }
}
