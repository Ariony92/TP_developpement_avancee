package tp_avancee_dev.tp_avancee.api.security;

import tp_avancee_dev.tp_avancee.service.AuthService;

import javax.security.auth.callback.Callback;

public class AuthServiceCallback implements Callback {

    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
