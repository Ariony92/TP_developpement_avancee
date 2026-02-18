package tp_avancee_dev.tp_avancee.api;

import jakarta.validation.Valid;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp_avancee_dev.tp_avancee.api.dto.LoginRequestDto;
import tp_avancee_dev.tp_avancee.api.dto.LoginResponseDto;
import tp_avancee_dev.tp_avancee.api.log.StructuredLogger;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.service.ApiTokenService;
import tp_avancee_dev.tp_avancee.service.AuthService;

import java.util.Map;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

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
        User user = authService.login(request.getLogin(), request.getPassword());
        if (user == null) {
            StructuredLogger.info("api.login.failed", Map.of("login", request.getLogin()));
            throw new NotAuthorizedException("Identifiants invalides");
        }

        String token = apiTokenService.createToken(user);

        LoginResponseDto payload = new LoginResponseDto();
        payload.setTokenType("Bearer");
        payload.setAccessToken(token);
        payload.setUserId(user.getId());
        payload.setUsername(user.getUsername());

        StructuredLogger.info("api.login.success", Map.of("userId", user.getId(), "username", user.getUsername()));
        return Response.ok(payload).build();
    }
}
