package tp_avancee_dev.tp_avancee.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import tp_avancee_dev.tp_avancee.dto.AnnonceRequestDto;
import tp_avancee_dev.tp_avancee.dto.AnnonceResponseDto;
import tp_avancee_dev.tp_avancee.dto.StatusPatchDto;
import tp_avancee_dev.tp_avancee.exception.AccessDeniedException;
import tp_avancee_dev.tp_avancee.exception.BusinessConflictException;
import tp_avancee_dev.tp_avancee.exception.ResourceNotFoundException;
import tp_avancee_dev.tp_avancee.mapper.AnnonceMapper;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.model.Category;
import tp_avancee_dev.tp_avancee.model.Status;
import tp_avancee_dev.tp_avancee.model.User;
import tp_avancee_dev.tp_avancee.repository.AnnonceRepository;
import tp_avancee_dev.tp_avancee.repository.CategoryRepository;
import tp_avancee_dev.tp_avancee.repository.UserRepository;
import tp_avancee_dev.tp_avancee.security.CustomUserPrincipal;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock
    private AnnonceRepository annonceRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AnnonceMapper annonceMapper;

    @InjectMocks
    private AnnonceService annonceService;

    private User testUser;
    private User otherUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("USER");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        otherUser.setRole("USER");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setLabel("Immobilier");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(Long userId, String username, String role) {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                userId, username, "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Annonce buildAnnonce(Long id, Status status, User author) {
        Annonce a = new Annonce();
        a.setId(id);
        a.setVersion(0L);
        a.setTitle("Titre");
        a.setDescription("Description");
        a.setAdress("Adresse");
        a.setMail("test@test.com");
        a.setStatus(status);
        a.setAuthor(author);
        a.setCategory(testCategory);
        return a;
    }

    private AnnonceRequestDto buildRequest() {
        AnnonceRequestDto dto = new AnnonceRequestDto();
        dto.setTitle("Nouveau titre");
        dto.setDescription("Nouvelle description");
        dto.setAdress("Nouvelle adresse");
        dto.setMail("new@test.com");
        dto.setCategoryId(1L);
        return dto;
    }

    // ──────────────────────────────────────────────
    // getById
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("retourne le DTO quand l'annonce existe")
        void returnsDto_whenFound() {
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);
            AnnonceResponseDto expected = new AnnonceResponseDto();
            expected.setId(1L);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));
            when(annonceMapper.toDto(annonce)).thenReturn(expected);

            AnnonceResponseDto result = annonceService.getById(1L);

            assertEquals(1L, result.getId());
            verify(annonceRepository).findWithRelationsById(1L);
        }

        @Test
        @DisplayName("lève ResourceNotFoundException quand l'annonce n'existe pas")
        void throwsNotFound_whenMissing() {
            when(annonceRepository.findWithRelationsById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> annonceService.getById(99L));
        }
    }

    // ──────────────────────────────────────────────
    // create
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("crée une annonce en DRAFT pour l'utilisateur authentifié")
        void createsAnnonce_asAuthenticated() {
            authenticateAs(1L, "testuser", "USER");
            AnnonceRequestDto request = buildRequest();
            Annonce entity = buildAnnonce(null, Status.DRAFT, testUser);
            Annonce saved = buildAnnonce(1L, Status.DRAFT, testUser);
            AnnonceResponseDto expected = new AnnonceResponseDto();
            expected.setId(1L);
            expected.setStatus(Status.DRAFT);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(annonceMapper.toEntity(request)).thenReturn(entity);
            when(annonceRepository.save(any(Annonce.class))).thenReturn(saved);
            when(annonceMapper.toDto(saved)).thenReturn(expected);

            AnnonceResponseDto result = annonceService.create(request);

            assertEquals(Status.DRAFT, result.getStatus());
            verify(annonceRepository).save(any(Annonce.class));
        }

        @Test
        @DisplayName("lève IllegalArgumentException si la catégorie n'existe pas")
        void throwsBadRequest_whenCategoryNotFound() {
            authenticateAs(1L, "testuser", "USER");
            AnnonceRequestDto request = buildRequest();
            request.setCategoryId(999L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> annonceService.create(request));
        }
    }

    // ──────────────────────────────────────────────
    // update
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("met à jour quand l'utilisateur est l'auteur")
        void updatesAnnonce_whenOwner() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);
            AnnonceRequestDto request = buildRequest();
            AnnonceResponseDto expected = new AnnonceResponseDto();
            expected.setId(1L);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
            when(annonceMapper.toDto(annonce)).thenReturn(expected);

            AnnonceResponseDto result = annonceService.update(1L, request);

            assertNotNull(result);
            verify(annonceMapper).updateEntity(request, annonce);
        }

        @Test
        @DisplayName("lève AccessDeniedException quand l'utilisateur n'est pas l'auteur")
        void throwsForbidden_whenNotOwner() {
            authenticateAs(2L, "otheruser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(AccessDeniedException.class,
                    () -> annonceService.update(1L, buildRequest()));
        }

        @Test
        @DisplayName("un ADMIN peut modifier l'annonce d'un autre")
        void allowsUpdate_whenAdmin() {
            authenticateAs(2L, "admin", "ADMIN");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);
            AnnonceRequestDto request = buildRequest();
            AnnonceResponseDto expected = new AnnonceResponseDto();

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
            when(annonceMapper.toDto(annonce)).thenReturn(expected);

            assertDoesNotThrow(() -> annonceService.update(1L, request));
        }

        @Test
        @DisplayName("lève BusinessConflictException quand l'annonce est PUBLISHED")
        void throwsConflict_whenPublished() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.PUBLISHED, testUser);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(BusinessConflictException.class,
                    () -> annonceService.update(1L, buildRequest()));
        }

        @Test
        @DisplayName("lève BusinessConflictException en cas de conflit de version")
        void throwsConflict_whenVersionMismatch() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);
            annonce.setVersion(5L);
            AnnonceRequestDto request = buildRequest();
            request.setVersion(3L);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(BusinessConflictException.class,
                    () -> annonceService.update(1L, request));
        }

        @Test
        @DisplayName("lève ResourceNotFoundException quand l'annonce n'existe pas")
        void throwsNotFound_whenMissing() {
            authenticateAs(1L, "testuser", "USER");

            when(annonceRepository.findWithRelationsById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> annonceService.update(99L, buildRequest()));
        }
    }

    // ──────────────────────────────────────────────
    // delete
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("supprime quand l'annonce est ARCHIVED et l'utilisateur est l'auteur")
        void deletesAnnonce_whenArchivedAndOwner() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.ARCHIVED, testUser);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertDoesNotThrow(() -> annonceService.delete(1L));
            verify(annonceRepository).delete(annonce);
        }

        @Test
        @DisplayName("lève BusinessConflictException quand l'annonce n'est pas ARCHIVED")
        void throwsConflict_whenNotArchived() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(BusinessConflictException.class, () -> annonceService.delete(1L));
            verify(annonceRepository, never()).delete(any());
        }

        @Test
        @DisplayName("lève AccessDeniedException quand l'utilisateur n'est pas l'auteur")
        void throwsForbidden_whenNotOwner() {
            authenticateAs(2L, "otheruser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.ARCHIVED, testUser);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(AccessDeniedException.class, () -> annonceService.delete(1L));
        }
    }

    // ──────────────────────────────────────────────
    // changeStatus
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatus {

        @Test
        @DisplayName("un ADMIN peut archiver une annonce")
        void adminCanArchive() {
            authenticateAs(2L, "admin", "ADMIN");
            Annonce annonce = buildAnnonce(1L, Status.PUBLISHED, testUser);
            StatusPatchDto patch = new StatusPatchDto();
            patch.setStatus(Status.ARCHIVED);
            AnnonceResponseDto expected = new AnnonceResponseDto();
            expected.setStatus(Status.ARCHIVED);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));
            when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
            when(annonceMapper.toDto(annonce)).thenReturn(expected);

            AnnonceResponseDto result = annonceService.changeStatus(1L, patch);

            assertEquals(Status.ARCHIVED, result.getStatus());
        }

        @Test
        @DisplayName("un USER ne peut pas archiver")
        void userCannotArchive() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);
            StatusPatchDto patch = new StatusPatchDto();
            patch.setStatus(Status.ARCHIVED);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(AccessDeniedException.class,
                    () -> annonceService.changeStatus(1L, patch));
        }

        @Test
        @DisplayName("l'auteur peut publier sa propre annonce")
        void ownerCanPublish() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);
            StatusPatchDto patch = new StatusPatchDto();
            patch.setStatus(Status.PUBLISHED);
            AnnonceResponseDto expected = new AnnonceResponseDto();
            expected.setStatus(Status.PUBLISHED);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));
            when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
            when(annonceMapper.toDto(annonce)).thenReturn(expected);

            AnnonceResponseDto result = annonceService.changeStatus(1L, patch);

            assertEquals(Status.PUBLISHED, result.getStatus());
        }

        @Test
        @DisplayName("lève BusinessConflictException quand le statut est identique")
        void throwsConflict_whenSameStatus() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.DRAFT, testUser);
            StatusPatchDto patch = new StatusPatchDto();
            patch.setStatus(Status.DRAFT);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(BusinessConflictException.class,
                    () -> annonceService.changeStatus(1L, patch));
        }

        @Test
        @DisplayName("lève BusinessConflictException si ARCHIVED → PUBLISHED")
        void throwsConflict_whenArchivedToPublished() {
            authenticateAs(1L, "testuser", "USER");
            Annonce annonce = buildAnnonce(1L, Status.ARCHIVED, testUser);
            StatusPatchDto patch = new StatusPatchDto();
            patch.setStatus(Status.PUBLISHED);

            when(annonceRepository.findWithRelationsById(1L)).thenReturn(Optional.of(annonce));

            assertThrows(BusinessConflictException.class,
                    () -> annonceService.changeStatus(1L, patch));
        }
    }
}
