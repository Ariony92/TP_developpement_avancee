package tp_avancee_dev.tp_avancee.api.security;

import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.service.AuthService;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

public class DbLoginModule implements LoginModule {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private Subject subject;
    private CallbackHandler callbackHandler;

    private boolean authenticationSucceeded;
    private UserPrincipal userPrincipal;
    private RolePrincipal userRolePrincipal;
    private RolePrincipal adminRolePrincipal;

    @Override
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map<String, ?> sharedState,
                           Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("CallbackHandler absent");
        }

        NameCallback nameCallback = new NameCallback("login");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        AuthServiceCallback authServiceCallback = new AuthServiceCallback();
        Callback[] callbacks = {nameCallback, passwordCallback, authServiceCallback};

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Impossible de traiter les callbacks JAAS: " + e.getMessage());
        }

        AuthService authService = authServiceCallback.getAuthService();
        if (authService == null) {
            throw new LoginException("Service d'authentification indisponible");
        }

        String login = nameCallback.getName();
        String password = passwordCallback.getPassword() == null ? null : new String(passwordCallback.getPassword());
        User user = authService.login(login, password);
        passwordCallback.clearPassword();

        if (user == null) {
            throw new FailedLoginException("Identifiants invalides");
        }

        userPrincipal = new UserPrincipal(user.getId(), user.getUsername());
        userRolePrincipal = new RolePrincipal(ROLE_USER);
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            adminRolePrincipal = new RolePrincipal(ROLE_ADMIN);
        }
        authenticationSucceeded = true;
        return true;
    }

    @Override
    public boolean commit() {
        if (!authenticationSucceeded || userPrincipal == null) {
            return false;
        }

        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(userRolePrincipal);
        if (adminRolePrincipal != null) {
            subject.getPrincipals().add(adminRolePrincipal);
        }
        return true;
    }

    @Override
    public boolean abort() {
        if (!authenticationSucceeded) {
            return false;
        }
        logout();
        return true;
    }

    @Override
    public boolean logout() {
        if (userPrincipal != null) {
            subject.getPrincipals().remove(userPrincipal);
        }
        if (userRolePrincipal != null) {
            subject.getPrincipals().remove(userRolePrincipal);
        }
        if (adminRolePrincipal != null) {
            subject.getPrincipals().remove(adminRolePrincipal);
        }

        authenticationSucceeded = false;
        userPrincipal = null;
        userRolePrincipal = null;
        adminRolePrincipal = null;
        return true;
    }
}
