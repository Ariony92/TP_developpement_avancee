package tp_avancee_dev.tp_avancee.api;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp_avancee_dev.tp_avancee.api.dto.LoginRequestDto;
import tp_avancee_dev.tp_avancee.api.dto.LoginResponseDto;
import tp_avancee_dev.tp_avancee.api.log.StructuredLogger;
import tp_avancee_dev.tp_avancee.api.security.AuthServiceCallback;
import tp_avancee_dev.tp_avancee.api.security.UserPrincipal;
import tp_avancee_dev.tp_avancee.service.ApiTokenService;
import tp_avancee_dev.tp_avancee.service.AuthService;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.Map;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String JAAS_LOGIN_DOMAIN = "MasterAnnonceLogin";

    private final AuthService authService;
    private final ApiTokenService apiTokenService;

    public AuthResource() {
        this(new AuthService(), ApiTokenService.getInstance());
    }

    public AuthResource(AuthService authService, ApiTokenService apiTokenService) {
        this.authService = authService;
        this.apiTokenService = apiTokenService;
    }

    @POST
    public Response login(@Valid LoginRequestDto request) {
        try {
            Subject subject = authenticateWithJaas(request.getLogin(), request.getPassword());
            UserPrincipal principal = subject.getPrincipals(UserPrincipal.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotAuthorizedException("Identifiants invalides"));

            String token = apiTokenService.createToken(principal.getUserId(), principal.getName());

            LoginResponseDto payload = new LoginResponseDto();
            payload.setTokenType("Bearer");
            payload.setAccessToken(token);
            payload.setUserId(principal.getUserId());
            payload.setUsername(principal.getName());

            StructuredLogger.info("api.login.success", Map.of("userId", principal.getUserId(), "username", principal.getName()));
            return Response.ok(payload).build();
        } catch (LoginException e) {
            StructuredLogger.info("api.login.failed", Map.of("login", request.getLogin()));
            throw new NotAuthorizedException("Identifiants invalides");
        }
    }

    private Subject authenticateWithJaas(String login, String password) throws LoginException {
        CallbackHandler callbackHandler = new LoginCallbackHandler(login, password, authService);
        LoginContext loginContext = new LoginContext(JAAS_LOGIN_DOMAIN, null, callbackHandler);
        loginContext.login();
        return loginContext.getSubject();
    }

    private static class LoginCallbackHandler implements CallbackHandler {
        private final String login;
        private final String password;
        private final AuthService authService;

        private LoginCallbackHandler(String login, String password, AuthService authService) {
            this.login = login;
            this.password = password;
            this.authService = authService;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(login);
                } else if (callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword(password == null ? new char[0] : password.toCharArray());
                } else if (callback instanceof AuthServiceCallback) {
                    ((AuthServiceCallback) callback).setAuthService(authService);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }
    }

}
