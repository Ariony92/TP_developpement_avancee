package tp_avancee_dev.tp_avancee;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.validation.ValidationFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tp_avancee_dev.tp_avancee.api.AnnonceResource;
import tp_avancee_dev.tp_avancee.api.AuthResource;
import tp_avancee_dev.tp_avancee.api.dto.AnnonceRequestDto;
import tp_avancee_dev.tp_avancee.api.exceptions.*;
import tp_avancee_dev.tp_avancee.api.filter.BearerAuthFilter;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.model.Status;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.service.AnnonceService;
import tp_avancee_dev.tp_avancee.service.ApiTokenService;
import tp_avancee_dev.tp_avancee.service.AuthService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiRestIT extends JerseyTest {

    private AuthService authService;
    private AnnonceService annonceService;

    @Override
    protected Application configure() {
        authService = mock(AuthService.class);
        annonceService = mock(AnnonceService.class);

        User authUser = new User();
        authUser.setId(1L);
        authUser.setUsername("alice");
        when(authService.login(eq("alice"), eq("secret"))).thenReturn(authUser);

        Annonce annonce = buildAnnonce(1L, Status.DRAFT);
        when(annonceService.listAnnoncesPaginated(anyInt(), anyInt())).thenReturn(List.of(annonce));
        when(annonceService.getAnnonceById(eq(1L))).thenReturn(annonce);
        when(annonceService.getAnnonceById(eq(999L))).thenReturn(null);

        return new ResourceConfig()
                .register(new AuthResource(authService, ApiTokenService.getInstance()))
                .register(new AnnonceResource(annonceService))
                .register(BearerAuthFilter.class)
                .register(JacksonFeature.class)
                .register(ValidationFeature.class)
                .register(ApiErrorResponse.class)
                .register(BadRequestExceptionMapper.class)
                .register(NotAuthorizedExceptionMapper.class)
                .register(NotFoundExceptionMapper.class)
                .register(IllegalArgumentExceptionMapper.class)
                .register(BusinessConflictExceptionMapper.class)
                .register(ConstraintViolationExceptionMapper.class)
                .register(ThrowableExceptionMapper.class)
                .register(AccessDeniedExceptionMapper.class);
    }

    @BeforeEach
    void clearTokenStore() {
        ApiTokenService.getInstance().clear();
    }

    @Test
    void login_shouldReturnBearerTokenPayload() {
        Response response = target("login")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(Map.of("login", "alice", "password", "secret")));

        assertEquals(200, response.getStatus());
        Map<?, ?> payload = response.readEntity(Map.class);
        assertEquals("Bearer", payload.get("tokenType"));
        assertNotNull(payload.get("accessToken"));
        assertEquals(1, ((Number) payload.get("userId")).intValue());
    }

    @Test
    void annonces_withoutToken_shouldReturn401JsonError() {
        Response response = target("annonces")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get();

        assertEquals(401, response.getStatus());
        Map<?, ?> payload = response.readEntity(Map.class);
        assertEquals("UNAUTHORIZED", payload.get("error"));
    }

    @Test
    void annonces_withToken_shouldReturn200AndItems() {
        String token = loginAndGetToken();

        Response response = target("annonces")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .get();

        assertEquals(200, response.getStatus());
        Map<?, ?> payload = response.readEntity(Map.class);
        assertEquals(1, ((Number) payload.get("count")).intValue());
        List<?> items = (List<?>) payload.get("items");
        assertFalse(items.isEmpty());
    }

    @Test
    void annonces_postInvalidPayload_shouldReturn400ValidationError() {
        String token = loginAndGetToken();

        AnnonceRequestDto dto = new AnnonceRequestDto();
        Response response = target("annonces")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .post(Entity.entity(dto, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
        Map<?, ?> payload = response.readEntity(Map.class);
        assertEquals("VALIDATION_ERROR", payload.get("error"));
    }

    @Test
    void annonces_detailNotFound_shouldReturn404JsonError() {
        String token = loginAndGetToken();

        Response response = target("annonces/999")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer " + token)
                .get();

        assertEquals(404, response.getStatus());
        Map<?, ?> payload = response.readEntity(Map.class);
        assertEquals("NOT_FOUND", payload.get("error"));
    }

    private String loginAndGetToken() {
        Response loginResponse = target("login")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(Map.of("login", "alice", "password", "secret")));

        assertEquals(200, loginResponse.getStatus());
        Map<?, ?> body = loginResponse.readEntity(Map.class);
        return (String) body.get("accessToken");
    }

    private Annonce buildAnnonce(Long id, Status status) {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");

        Category category = new Category();
        category.setId(2L);
        category.setLabel("Sport");

        Annonce annonce = new Annonce("Titre", "Desc", "Paris", "mail@test.com");
        annonce.setId(id);
        annonce.setStatus(status);
        annonce.setAuthor(user);
        annonce.setCategory(category);
        annonce.setVersion(0L);
        return annonce;
    }
}
