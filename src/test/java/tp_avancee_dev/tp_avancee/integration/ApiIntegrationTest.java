package tp_avancee_dev.tp_avancee.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.repository.AnnonceRepository;
import tp_avancee_dev.tp_avancee.repository.CategoryRepository;
import tp_avancee_dev.tp_avancee.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestcontainersConfig.class)
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AnnonceRepository annonceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User normalUser;
    private User adminUser;
    private Category category;

    @BeforeEach
    void setUp() {
        annonceRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        category = new Category();
        category.setLabel("Immobilier");
        category = categoryRepository.save(category);

        normalUser = new User();
        normalUser.setUsername("user1");
        normalUser.setEmail("user1@test.com");
        normalUser.setPassword(passwordEncoder.encode("password123"));
        normalUser.setRole("USER");
        normalUser = userRepository.save(normalUser);

        adminUser = new User();
        adminUser.setUsername("admin1");
        adminUser.setEmail("admin1@test.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole("ADMIN");
        adminUser = userRepository.save(adminUser);
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        String loginJson = """
                {"username": "%s", "password": "%s"}
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String createAnnonceJson() {
        return """
                {
                    "title": "Appartement T3",
                    "description": "Bel appartement lumineux",
                    "adress": "12 rue de la Paix, Paris",
                    "mail": "contact@test.com",
                    "categoryId": %d
                }
                """.formatted(category.getId());
    }

    // ──────────────────────────────────────────────
    // Exercice 8 — Authentification
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("Authentification")
    class Auth {

        @Test
        @DisplayName("POST /api/auth/login — login réussi retourne un token")
        void login_success() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "user1", "password": "password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.type").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").isNumber());
        }

        @Test
        @DisplayName("POST /api/auth/login — identifiants invalides → 401")
        void login_badCredentials() throws Exception {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username": "user1", "password": "wrongpassword"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST protégé sans token → 401")
        void protectedEndpoint_noToken() throws Exception {
            mockMvc.perform(post("/api/annonces")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createAnnonceJson()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST protégé avec token invalide → 401")
        void protectedEndpoint_invalidToken() throws Exception {
            mockMvc.perform(post("/api/annonces")
                            .header("Authorization", "Bearer token.invalide.ici")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createAnnonceJson()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/annonces est public (pas besoin de token)")
        void listAnnonces_isPublic() throws Exception {
            mockMvc.perform(get("/api/annonces"))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────
    // Exercice 8 — CRUD complet
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CRUD complet")
    class Crud {

        @Test
        @DisplayName("POST → GET → PUT → PATCH status → DELETE — cycle de vie complet")
        void fullCrudLifecycle() throws Exception {
            String token = loginAndGetToken("user1", "password123");
            String adminToken = loginAndGetToken("admin1", "admin123");

            // CREATE
            MvcResult createResult = mockMvc.perform(post("/api/annonces")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createAnnonceJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.status").value("DRAFT"))
                    .andExpect(jsonPath("$.title").value("Appartement T3"))
                    .andReturn();

            Long annonceId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            // READ
            mockMvc.perform(get("/api/annonces/{id}", annonceId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(annonceId))
                    .andExpect(jsonPath("$.title").value("Appartement T3"));

            // UPDATE
            String updateJson = """
                    {
                        "title": "Appartement T4 rénové",
                        "description": "Bel appartement rénové",
                        "adress": "15 avenue des Champs, Paris",
                        "mail": "nouveau@test.com",
                        "categoryId": %d
                    }
                    """.formatted(category.getId());

            mockMvc.perform(put("/api/annonces/{id}", annonceId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Appartement T4 rénové"));

            // PATCH STATUS → ARCHIVED (admin only)
            mockMvc.perform(patch("/api/annonces/{id}/status", annonceId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"status": "ARCHIVED"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ARCHIVED"));

            // DELETE
            mockMvc.perform(delete("/api/annonces/{id}", annonceId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            // VERIFY DELETED
            mockMvc.perform(get("/api/annonces/{id}", annonceId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST création + GET liste paginée")
        void createAndList() throws Exception {
            String token = loginAndGetToken("user1", "password123");

            mockMvc.perform(post("/api/annonces")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createAnnonceJson()))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/annonces")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    // ──────────────────────────────────────────────
    // Exercice 8 — Rôles et autorisations
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("Autorisations et rôles")
    class Authorization {

        @Test
        @DisplayName("un USER ne peut pas archiver → 403")
        void userCannotArchive() throws Exception {
            String token = loginAndGetToken("user1", "password123");

            MvcResult createResult = mockMvc.perform(post("/api/annonces")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createAnnonceJson()))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long annonceId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            mockMvc.perform(patch("/api/annonces/{id}/status", annonceId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"status": "ARCHIVED"}
                                    """))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("un autre USER ne peut pas modifier l'annonce d'un autre → 403")
        void otherUserCannotUpdate() throws Exception {
            String ownerToken = loginAndGetToken("user1", "password123");

            MvcResult createResult = mockMvc.perform(post("/api/annonces")
                            .header("Authorization", "Bearer " + ownerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createAnnonceJson()))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long annonceId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            User otherUser = new User();
            otherUser.setUsername("user2");
            otherUser.setEmail("user2@test.com");
            otherUser.setPassword(passwordEncoder.encode("password123"));
            otherUser.setRole("USER");
            userRepository.save(otherUser);

            String otherToken = loginAndGetToken("user2", "password123");

            String updateJson = """
                    {
                        "title": "Modifié par un autre",
                        "description": "Hack",
                        "adress": "Adresse",
                        "mail": "hack@test.com",
                        "categoryId": %d
                    }
                    """.formatted(category.getId());

            mockMvc.perform(put("/api/annonces/{id}", annonceId)
                            .header("Authorization", "Bearer " + otherToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("une annonce PUBLISHED ne peut pas être modifiée → 409")
        void publishedCannotBeUpdated() throws Exception {
            String userToken = loginAndGetToken("user1", "password123");
            String adminToken = loginAndGetToken("admin1", "admin123");

            MvcResult createResult = mockMvc.perform(post("/api/annonces")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createAnnonceJson()))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long annonceId = objectMapper.readTree(
                    createResult.getResponse().getContentAsString()).get("id").asLong();

            mockMvc.perform(patch("/api/annonces/{id}/status", annonceId)
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"status": "PUBLISHED"}
                                    """))
                    .andExpect(status().isOk());

            String updateJson = """
                    {
                        "title": "Tentative de modification",
                        "description": "Interdit",
                        "adress": "Adresse",
                        "mail": "test@test.com",
                        "categoryId": %d
                    }
                    """.formatted(category.getId());

            mockMvc.perform(put("/api/annonces/{id}", annonceId)
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isConflict());
        }
    }
}
