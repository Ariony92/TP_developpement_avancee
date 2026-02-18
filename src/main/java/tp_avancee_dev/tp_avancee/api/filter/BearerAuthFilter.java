package tp_avancee_dev.tp_avancee.api.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import tp_avancee_dev.tp_avancee.api.security.ApiTokenServiceCallback;
import tp_avancee_dev.tp_avancee.api.security.AuthenticatedUserPrincipal;
import tp_avancee_dev.tp_avancee.api.security.BearerTokenLoginModule;
import tp_avancee_dev.tp_avancee.api.security.TokenCallback;
import tp_avancee_dev.tp_avancee.service.ApiTokenService;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BearerAuthFilter implements ContainerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JAAS_TOKEN_DOMAIN = "MasterAnnonceToken";

    private final ApiTokenService apiTokenService = ApiTokenService.getInstance();

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorization = requestContext.getHeaderString("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new NotAuthorizedException("Token Bearer manquant");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            Subject subject = authenticateWithJaas(token);
            AuthenticatedUserPrincipal principal = subject.getPrincipals(AuthenticatedUserPrincipal.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotAuthorizedException("Principal JAAS introuvable"));

            SecurityContext original = requestContext.getSecurityContext();
            requestContext.setProperty("authUserId", principal.getUserId());
            requestContext.setProperty("authUsername", principal.getName());
            requestContext.setSecurityContext(new BearerSecurityContext(principal, original != null && original.isSecure()));
        } catch (LoginException e) {
            throw new NotAuthorizedException("Token invalide", e);
        }
    }

    private Subject authenticateWithJaas(String token) throws LoginException {
        CallbackHandler callbackHandler = new BearerTokenCallbackHandler(token, apiTokenService);
        LoginContext loginContext = new LoginContext(JAAS_TOKEN_DOMAIN, null, callbackHandler, new BearerLoginConfiguration());
        loginContext.login();
        return loginContext.getSubject();
    }

    private static class BearerTokenCallbackHandler implements CallbackHandler {

        private final String token;
        private final ApiTokenService apiTokenService;

        private BearerTokenCallbackHandler(String token, ApiTokenService apiTokenService) {
            this.token = token;
            this.apiTokenService = apiTokenService;
        }

        @Override
        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
            for (Callback callback : callbacks) {
                if (callback instanceof TokenCallback) {
                    ((TokenCallback) callback).setToken(token);
                } else if (callback instanceof ApiTokenServiceCallback) {
                    ((ApiTokenServiceCallback) callback).setApiTokenService(apiTokenService);
                } else {
                    throw new UnsupportedCallbackException(callback);
                }
            }
        }
    }

    private static class BearerLoginConfiguration extends Configuration {
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            AppConfigurationEntry entry = new AppConfigurationEntry(
                    BearerTokenLoginModule.class.getName(),
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    Map.of()
            );
            return new AppConfigurationEntry[]{entry};
        }
    }

    private static class BearerSecurityContext implements SecurityContext {

        private final AuthenticatedUserPrincipal principal;
        private final boolean secure;

        private BearerSecurityContext(AuthenticatedUserPrincipal principal, boolean secure) {
            this.principal = principal;
            this.secure = secure;
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return "BEARER";
        }
    }
}
