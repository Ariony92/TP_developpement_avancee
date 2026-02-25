package tp_avancee_dev.tp_avancee.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import tp_avancee_dev.tp_avancee.specification.AnnonceSpecification;

import java.time.Instant;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AnnonceMapper annonceMapper;

    public AnnonceService(AnnonceRepository annonceRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          AnnonceMapper annonceMapper) {
        this.annonceRepository = annonceRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.annonceMapper = annonceMapper;
    }

    public AnnonceResponseDto getById(Long id) {
        Annonce annonce = annonceRepository.findWithRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable : id=" + id));
        return annonceMapper.toDto(annonce);
    }

    public Page<AnnonceResponseDto> search(String q,
                                            Status status,
                                            Long categoryId,
                                            Long authorId,
                                            Instant fromDate,
                                            Instant toDate,
                                            Pageable pageable) {

        Specification<Annonce> spec = Specification.where(AnnonceSpecification.fetchRelations());

        if (status != null) {
            spec = spec.and(AnnonceSpecification.hasStatus(status));
        }
        if (categoryId != null) {
            spec = spec.and(AnnonceSpecification.hasCategoryId(categoryId));
        }
        if (authorId != null) {
            spec = spec.and(AnnonceSpecification.hasAuthorId(authorId));
        }
        if (q != null && !q.isBlank()) {
            spec = spec.and(AnnonceSpecification.searchOnStringFields(q));
        }
        if (fromDate != null) {
            spec = spec.and(AnnonceSpecification.dateAfter(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(AnnonceSpecification.dateBefore(toDate));
        }

        return annonceRepository.findAll(spec, pageable)
                .map(annonceMapper::toDto);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public AnnonceResponseDto create(AnnonceRequestDto request) {
        CustomUserPrincipal principal = getCurrentUser();

        User author = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Auteur introuvable : id=" + principal.getUserId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable : id=" + request.getCategoryId()));

        Annonce annonce = annonceMapper.toEntity(request);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        annonce.setStatus(Status.DRAFT);

        Annonce saved = annonceRepository.save(annonce);
        return annonceMapper.toDto(saved);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public AnnonceResponseDto update(Long id, AnnonceRequestDto request) {
        Annonce annonce = annonceRepository.findWithRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable : id=" + id));

        checkOwnership(annonce);

        if (annonce.getStatus() == Status.PUBLISHED) {
            throw new BusinessConflictException("Une annonce PUBLISHED ne peut plus être modifiée");
        }

        if (request.getVersion() != null && !Objects.equals(annonce.getVersion(), request.getVersion())) {
            throw new BusinessConflictException(
                    "Conflit de concurrence : version attendue=" + request.getVersion()
                            + ", version actuelle=" + annonce.getVersion());
        }

        annonceMapper.updateEntity(request, annonce);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable : id=" + request.getCategoryId()));
        annonce.setCategory(category);

        Annonce updated = annonceRepository.save(annonce);
        return annonceMapper.toDto(updated);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void delete(Long id) {
        Annonce annonce = annonceRepository.findWithRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable : id=" + id));

        checkOwnership(annonce);

        if (annonce.getStatus() != Status.ARCHIVED) {
            throw new BusinessConflictException("Archivage obligatoire avant suppression");
        }

        annonceRepository.delete(annonce);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public AnnonceResponseDto changeStatus(Long id, StatusPatchDto request) {
        Annonce annonce = annonceRepository.findWithRelationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce introuvable : id=" + id));

        Status newStatus = request.getStatus();

        if (newStatus == Status.ARCHIVED) {
            checkAdmin();
        } else {
            checkOwnership(annonce);
        }

        if (annonce.getStatus() == newStatus) {
            throw new BusinessConflictException("Le statut est déjà " + newStatus);
        }
        if (annonce.getStatus() == Status.ARCHIVED && newStatus == Status.PUBLISHED) {
            throw new BusinessConflictException("Une annonce ARCHIVED ne peut pas repasser en PUBLISHED");
        }

        annonce.setStatus(newStatus);
        Annonce updated = annonceRepository.save(annonce);
        return annonceMapper.toDto(updated);
    }

    private CustomUserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserPrincipal)) {
            throw new AccessDeniedException("Utilisateur non authentifié");
        }
        return (CustomUserPrincipal) auth.getPrincipal();
    }

    private void checkOwnership(Annonce annonce) {
        CustomUserPrincipal principal = getCurrentUser();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !Objects.equals(annonce.getAuthor().getId(), principal.getUserId())) {
            throw new AccessDeniedException("Seul l'auteur peut modifier cette annonce");
        }
    }

    private void checkAdmin() {
        CustomUserPrincipal principal = getCurrentUser();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Seul un ADMIN peut archiver une annonce");
        }
    }
}
