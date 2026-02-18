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
import tp_avancee_dev.tp_avancee.api.exceptions.*;
import tp_avancee_dev.tp_avancee.api.filter.BearerAuthFilter;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.model.Status;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.service.AnnonceService;
import tp_avancee_dev.tp_avancee.service.ApiTokenService;
import tp_avancee_dev.tp_avancee.service.AuthService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoadTestIT extends JerseyTest {

    private static final int CONCURRENT_USERS = 20;
    private static final int REQUESTS_PER_USER = 5;
    private static final long MAX_AVG_RESPONSE_MS = 500;

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

        return new ResourceConfig()
                .register(new AuthResource(authService, ApiTokenService.getInstance()))
                .register(new AnnonceResource(annonceService))
                .register(BearerAuthFilter.class)
                .register(JacksonFeature.class)
                .register(ValidationFeature.class)
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
    void loadTest_concurrentGetAnnonces_shouldHandleMultipleUsers() throws Exception {
        String token = loginAndGetToken();
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Long> responseTimes = new ArrayList<>();

        int totalRequests = CONCURRENT_USERS * REQUESTS_PER_USER;
        List<Callable<Long>> tasks = new ArrayList<>();

        for (int i = 0; i < totalRequests; i++) {
            tasks.add(() -> {
                long start = System.currentTimeMillis();
                Response response = target("annonces")
                        .queryParam("page", 1)
                        .queryParam("size", 10)
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("Authorization", "Bearer " + token)
                        .get();
                long elapsed = System.currentTimeMillis() - start;

                if (response.getStatus() == 200) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                }
                response.close();
                return elapsed;
            });
        }

        List<Future<Long>> futures = executor.invokeAll(tasks);
        for (Future<Long> future : futures) {
            responseTimes.add(future.get());
        }
        executor.shutdown();

        long totalTime = responseTimes.stream().mapToLong(Long::longValue).sum();
        long avgTime = totalTime / responseTimes.size();
        long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("=== RAPPORT TEST DE CHARGE ===");
        System.out.println("Requêtes totales : " + totalRequests);
        System.out.println("Succès            : " + successCount.get());
        System.out.println("Échecs            : " + failCount.get());
        System.out.println("Temps moyen       : " + avgTime + " ms");
        System.out.println("Temps min         : " + minTime + " ms");
        System.out.println("Temps max         : " + maxTime + " ms");
        System.out.println("==============================");

        assertEquals(totalRequests, successCount.get(), "Toutes les requêtes doivent réussir");
        assertEquals(0, failCount.get(), "Aucune requête ne doit échouer");
        assertTrue(avgTime < MAX_AVG_RESPONSE_MS,
                "Le temps de réponse moyen (" + avgTime + " ms) dépasse le seuil (" + MAX_AVG_RESPONSE_MS + " ms)");
    }

    @Test
    void loadTest_concurrentLogin_shouldHandleMultipleAuthentications() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Long> responseTimes = new ArrayList<>();

        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            tasks.add(() -> {
                long start = System.currentTimeMillis();
                Response response = target("login")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.json(Map.of("username", "alice", "password", "secret")));
                long elapsed = System.currentTimeMillis() - start;

                if (response.getStatus() == 200) {
                    successCount.incrementAndGet();
                }
                response.close();
                return elapsed;
            });
        }

        List<Future<Long>> futures = executor.invokeAll(tasks);
        for (Future<Long> future : futures) {
            responseTimes.add(future.get());
        }
        executor.shutdown();

        long avgTime = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();

        System.out.println("=== RAPPORT TEST DE CHARGE LOGIN ===");
        System.out.println("Logins simultanés : " + CONCURRENT_USERS);
        System.out.println("Succès             : " + successCount.get());
        System.out.println("Temps moyen        : " + avgTime + " ms");
        System.out.println("====================================");

        assertEquals(CONCURRENT_USERS, successCount.get(), "Tous les logins doivent réussir");
    }

    @Test
    void loadTest_unauthenticatedRequests_shouldAll401() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        AtomicInteger count401 = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS * 2; i++) {
            tasks.add(() -> {
                Response response = target("annonces")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get();
                if (response.getStatus() == 401) {
                    count401.incrementAndGet();
                }
                response.close();
                return null;
            });
        }

        executor.invokeAll(tasks);
        executor.shutdown();

        assertEquals(CONCURRENT_USERS * 2, count401.get(),
                "Toutes les requêtes sans token doivent retourner 401");
    }

    private String loginAndGetToken() {
        Response loginResponse = target("login")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(Map.of("username", "alice", "password", "secret")));

        assertEquals(200, loginResponse.getStatus());
        Map<?, ?> body = loginResponse.readEntity(Map.class);
        Object token = body.get("token");
        return token instanceof String ? (String) token : (String) body.get("accessToken");
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
