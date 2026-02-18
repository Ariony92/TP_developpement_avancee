package tp_avancee_dev.tp_avancee.api.filter;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import tp_avancee_dev.tp_avancee.api.security.ApiTokenServiceCallback;
import tp_avancee_dev.tp_avancee.api.security.BearerTokenLoginModule;
import tp_avancee_dev.tp_avancee.api.security.RolePrincipal;
import tp_avancee_dev.tp_avancee.api.security.TokenCallback;
import tp_avancee_dev.tp_avancee.api.security.UserPrincipal;
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
import java.util.Set;
import java.util.stream.Collectors;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BearerAuthFilter implements ContainerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JAAS_TOKEN_DOMAIN = "MasterAnnonceToken";

    private final ApiTokenService apiTokenService = ApiTokenService.getInstance();

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorization = requestContext.getHeaderString("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new NotAuthorizedException("Token Bearer manquant");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            Subject subject = authenticateWithJaas(token);
            UserPrincipal principal = subject.getPrincipals(UserPrincipal.class)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new NotAuthorizedException("Principal JAAS introuvable"));

            Set<String> roles = subject.getPrincipals(RolePrincipal.class)
                    .stream()
                    .map(RolePrincipal::getName)
                    .collect(Collectors.toSet());

            requestContext.setProperty("authUserId", principal.getUserId());
            requestContext.setProperty("authUsername", principal.getName());
            requestContext.setProperty("subject", subject);
            if (httpServletRequest != null) {
                httpServletRequest.setAttribute("subject", subject);
            }
            SecurityContext original = requestContext.getSecurityContext();
            boolean isSecure = original != null && original.isSecure();
            requestContext.setSecurityContext(new BearerSecurityContext(principal, roles, isSecure));

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

        private final UserPrincipal principal;
        private final Set<String> roles;
        private final boolean secure;

        private BearerSecurityContext(UserPrincipal principal, Set<String> roles, boolean secure) {
            this.principal = principal;
            this.roles = roles;
            this.secure = secure;
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return roles.contains(role);
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
