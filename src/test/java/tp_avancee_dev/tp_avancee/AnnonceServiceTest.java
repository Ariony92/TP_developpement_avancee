package tp_avancee_dev.tp_avancee;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tp_avancee_dev.tp_avancee.db.EntityManagerUtil;
import tp_avancee_dev.tp_avancee.model.*;
import tp_avancee_dev.tp_avancee.repository.AnnonceRepository;
import tp_avancee_dev.tp_avancee.service.AnnonceService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnonceServiceTest {

    @Test
    void publishAnnonce_shouldUpdateStatusAndCommitTransaction() {
        AnnonceRepository repository = mock(AnnonceRepository.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);

        Annonce annonce = new Annonce();
        annonce.setId(10L);
        annonce.setStatus(Status.DRAFT);

        when(em.getTransaction()).thenReturn(tx);
        when(repository.findById(em, 10L)).thenReturn(annonce);
        when(repository.update(em, annonce)).thenAnswer(invocation -> invocation.getArgument(1));

        AnnonceService service = new AnnonceService(repository);

        try (MockedStatic<EntityManagerUtil> mockedUtil = mockStatic(EntityManagerUtil.class)) {
            mockedUtil.when(EntityManagerUtil::createEntityManager).thenReturn(em);

            Annonce result = service.publishAnnonce(10L);

            assertEquals(Status.PUBLISHED, result.getStatus());
            verify(tx).begin();
            verify(repository).findById(em, 10L);
            verify(repository).update(em, annonce);
            verify(tx).commit();
            verify(em).close();
        }
    }

    @Test
    void createAnnonce_shouldSetDraftAndCallCreate() {
        AnnonceRepository repository = mock(AnnonceRepository.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);

        User author = new User();
        author.setId(1L);
        Category category = new Category();
        category.setId(2L);

        when(em.getTransaction()).thenReturn(tx);
        when(em.find(User.class, 1L)).thenReturn(author);
        when(em.find(Category.class, 2L)).thenReturn(category);

        AnnonceService service = new AnnonceService(repository);

        try (MockedStatic<EntityManagerUtil> mockedUtil = mockStatic(EntityManagerUtil.class)) {
            mockedUtil.when(EntityManagerUtil::createEntityManager).thenReturn(em);

            Annonce created = service.createAnnonce("Titre", "Desc", "Paris", "a@b.com", 1L, 2L);

            assertEquals(Status.DRAFT, created.getStatus());
            verify(repository).create(eq(em), any(Annonce.class));
            verify(tx).commit();
            verify(em).close();
        }
    }

    @Test
    void createAnnonce_shouldRollbackWhenAuthorMissing() {
        AnnonceRepository repository = mock(AnnonceRepository.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);

        when(em.getTransaction()).thenReturn(tx);
        when(em.find(User.class, 999L)).thenReturn(null);
        when(tx.isActive()).thenReturn(true);

        AnnonceService service = new AnnonceService(repository);

        try (MockedStatic<EntityManagerUtil> mockedUtil = mockStatic(EntityManagerUtil.class)) {
            mockedUtil.when(EntityManagerUtil::createEntityManager).thenReturn(em);

            assertThrows(IllegalArgumentException.class,
                    () -> service.createAnnonce("Titre", "Desc", "Paris", "a@b.com", 999L, 2L));

            verify(tx).begin();
            verify(tx).rollback();
            verify(repository, never()).create(eq(em), any(Annonce.class));
            verify(em).close();
        }
    }

    @Test
    void archiveAnnonce_shouldUpdateStatusAndCommitTransaction() {
        AnnonceRepository repository = mock(AnnonceRepository.class);
        EntityManager em = mock(EntityManager.class);
        EntityTransaction tx = mock(EntityTransaction.class);

        Annonce annonce = new Annonce();
        annonce.setId(15L);
        annonce.setStatus(Status.PUBLISHED);

        when(em.getTransaction()).thenReturn(tx);
        when(repository.findById(em, 15L)).thenReturn(annonce);
        when(repository.update(em, annonce)).thenAnswer(invocation -> invocation.getArgument(1));

        AnnonceService service = new AnnonceService(repository);

        try (MockedStatic<EntityManagerUtil> mockedUtil = mockStatic(EntityManagerUtil.class)) {
            mockedUtil.when(EntityManagerUtil::createEntityManager).thenReturn(em);

            Annonce result = service.archiveAnnonce(15L);

            assertEquals(Status.ARCHIVED, result.getStatus());
            verify(tx).begin();
            verify(repository).findById(em, 15L);
            verify(repository).update(em, annonce);
            verify(tx).commit();
            verify(em).close();
        }
    }
}
