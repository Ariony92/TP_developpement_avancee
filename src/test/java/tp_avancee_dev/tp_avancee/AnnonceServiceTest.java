package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tp_avancee_dev.tp_avancee.api.exceptions.AccessDeniedException;
import tp_avancee_dev.tp_avancee.api.security.RolePrincipal;
import tp_avancee_dev.tp_avancee.api.security.UserPrincipal;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.*;
import tp_avancee_dev.tp_avancee.repository.AnnonceRepository;
import tp_avancee_dev.tp_avancee.service.AnnonceService;

import javax.security.auth.Subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnonceServiceTest {

    @Test
    void changeStatusTo_shouldUpdateStatusAndCommitTransaction_whenRequesterIsAuthor() {
        AnnonceRepository repository = mock(AnnonceRepository.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setStatus(Status.DRAFT);
        User author = new User();
        author.setId(99L);
        annonce.setAuthor(author);

        when(em.getTransaction()).thenReturn(tx);
        when(repository.findById(em, 10L)).thenReturn(annonce);
        when(repository.update(em, annonce)).thenAnswer(invocation -> invocation.getArgument(1));

        AnnonceService service = new AnnonceService(repository);

        try (MockedStatic<EntityManagerUtil> mockedUtil = mockStatic(EntityManagerUtil.class)) {
            mockedUtil.when(EntityManagerUtil::createEntityManager).thenReturn(em);

            Annonce result = service.changeStatusTo(10L, Status.PUBLISHED, 99L);

            assertEquals(Status.PUBLISHED, result.getStatus());
            verify(tx).begin();
            verify(repository).findById(em, 10L);
            verify(repository).update(em, annonce);
            verify(tx).commit();
            verify(em).close();
        }
    }

    @Test
    void updateAnnonce_shouldExtractUserFromSubjectAndBlockNonAuthor() {
        AnnonceRepository repository = mock(AnnonceRepository.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);

        User author = new User();
        author.setId(2L);

        Category category = new Category();
        category.setId(7L);

        Annonce annonce = new Annonce();
        annonce.setId(55L);
        annonce.setTitle("old");
        annonce.setDescription("old");
        annonce.setAdress("old");
        annonce.setMail("old@test.com");
        annonce.setStatus(Status.DRAFT);
        annonce.setAuthor(author);
        annonce.setCategory(category);

        Subject subject = new Subject();
        subject.getPrincipals().add(new UserPrincipal(1L, "alice"));

        when(em.getTransaction()).thenReturn(tx);
        when(repository.findById(em, 55L)).thenReturn(annonce);
        when(tx.isActive()).thenReturn(true);

        AnnonceService service = new AnnonceService(repository);

        try (MockedStatic<EntityManagerUtil> mockedUtil = mockStatic(EntityManagerUtil.class)) {
            mockedUtil.when(EntityManagerUtil::createEntityManager).thenReturn(em);

            assertThrows(AccessDeniedException.class,
                    () -> service.updateAnnonce(55L, "new", "new", "new", "new@test.com", 7L, subject, null));

            verify(tx).rollback();
            verify(em).close();
        }
    }

    @Test
    void updateAnnonce_shouldAllowAdminRoleFromSubject() {
        AnnonceRepository repository = mock(AnnonceRepository.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);

        User author = new User();
        author.setId(2L);

        Category category = new Category();
        category.setId(7L);

        Annonce annonce = new Annonce();
        annonce.setId(56L);
        annonce.setTitle("old");
        annonce.setDescription("old");
        annonce.setAdress("old");
        annonce.setMail("old@test.com");
        annonce.setStatus(Status.DRAFT);
        annonce.setAuthor(author);
        annonce.setCategory(category);

        Subject subject = new Subject();
        subject.getPrincipals().add(new UserPrincipal(1L, "admin"));
        subject.getPrincipals().add(new RolePrincipal("ROLE_ADMIN"));

        when(em.getTransaction()).thenReturn(tx);
        when(repository.findById(em, 56L)).thenReturn(annonce);
        when(em.find(Category.class, 7L)).thenReturn(category);
        when(repository.update(em, annonce)).thenAnswer(invocation -> invocation.getArgument(1));

        AnnonceService service = new AnnonceService(repository);

        try (MockedStatic<EntityManagerUtil> mockedUtil = mockStatic(EntityManagerUtil.class)) {
            mockedUtil.when(EntityManagerUtil::createEntityManager).thenReturn(em);

            Annonce updated = service.updateAnnonce(56L, "new", "new", "new", "new@test.com", 7L, subject, null);

            assertEquals("new", updated.getTitle());
            verify(tx).commit();
            verify(em).close();
        }
    }

}
