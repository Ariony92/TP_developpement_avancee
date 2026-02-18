package tp_avancee_dev.tp_avancee.api.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import tp_avancee_dev.tp_avancee.service.ApiTokenService;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BearerAuthFilter implements ContainerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiTokenService apiTokenService = ApiTokenService.getInstance();

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorization = requestContext.getHeaderString("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new NotAuthorizedException("Token Bearer manquant");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        ApiTokenService.TokenSession session = apiTokenService.findSession(token)
                .orElseThrow(() -> new NotAuthorizedException("Token invalide"));

        requestContext.setProperty("authUserId", session.getUserId());
        requestContext.setProperty("authUsername", session.getUsername());
    }
}