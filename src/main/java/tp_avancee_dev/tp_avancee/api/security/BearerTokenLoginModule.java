package tp_avancee_dev.tp_avancee.api.security;

import tp_avancee_dev.tp_avancee.service.ApiTokenService;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

public class BearerTokenLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;

    private boolean authenticationSucceeded;
    private AuthenticatedUserPrincipal authenticatedPrincipal;

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

        TokenCallback tokenCallback = new TokenCallback();
        ApiTokenServiceCallback tokenServiceCallback = new ApiTokenServiceCallback();
        Callback[] callbacks = {tokenCallback, tokenServiceCallback};

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Impossible de traiter les callbacks JAAS: " + e.getMessage());
        }

        String token = tokenCallback.getToken();
        ApiTokenService tokenService = tokenServiceCallback.getApiTokenService();
        if (tokenService == null) {
            throw new LoginException("Service de token indisponible");
        }

        ApiTokenService.TokenSession session = tokenService.findSession(token)
                .orElseThrow(() -> new FailedLoginException("Token invalide"));

        authenticatedPrincipal = new AuthenticatedUserPrincipal(session.getUserId(), session.getUsername());
        authenticationSucceeded = true;
        return true;
    }

    @Override
    public boolean commit() {
        if (!authenticationSucceeded || authenticatedPrincipal == null) {
            return false;
        }

        if (!subject.getPrincipals().contains(authenticatedPrincipal)) {
            subject.getPrincipals().add(authenticatedPrincipal);
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
        if (authenticatedPrincipal != null) {
            subject.getPrincipals().remove(authenticatedPrincipal);
        }
        authenticationSucceeded = false;
        authenticatedPrincipal = null;
        return true;
    }
}
