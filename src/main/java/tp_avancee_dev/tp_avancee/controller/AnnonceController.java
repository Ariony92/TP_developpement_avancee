package tp_avancee_dev.tp_avancee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tp_avancee_dev.tp_avancee.dto.AnnonceRequestDto;
import tp_avancee_dev.tp_avancee.dto.AnnonceResponseDto;
import tp_avancee_dev.tp_avancee.dto.StatusPatchDto;
import tp_avancee_dev.tp_avancee.exception.ApiErrorResponse;
import tp_avancee_dev.tp_avancee.model.Status;
import tp_avancee_dev.tp_avancee.service.AnnonceService;
import tp_avancee_dev.tp_avancee.specification.AnnonceSpecification;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/annonces")
@Tag(name = "Annonces", description = "CRUD et recherche d'annonces")
public class AnnonceController {

    private final AnnonceService annonceService;

    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @GetMapping
    @Operation(
            summary = "Liste paginée des annonces",
            description = "Recherche multi-critères avec pagination et tri. Endpoint public.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Liste paginée"),
                    @ApiResponse(responseCode = "400", description = "Paramètre de tri invalide",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<Page<AnnonceResponseDto>> list(
            @Parameter(description = "Mot-clé (recherche LIKE sur title, description, address, mail)")
            @RequestParam(required = false) String q,
            @Parameter(description = "Filtre par statut (DRAFT, PUBLISHED, ARCHIVED)")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Filtre par ID de catégorie")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filtre par ID d'auteur")
            @RequestParam(required = false) Long authorId,
            @Parameter(description = "Date de début (ISO 8601, ex: 2025-01-01T00:00:00Z)")
            @RequestParam(required = false) Instant fromDate,
            @Parameter(description = "Date de fin (ISO 8601, ex: 2025-12-31T23:59:59Z)")
            @RequestParam(required = false) Instant toDate,
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable) {

        AnnonceSpecification.validateSortFields(
                pageable.getSort().stream()
                        .map(Sort.Order::getProperty)
                        .toArray(String[]::new));

        Page<AnnonceResponseDto> page = annonceService.search(q, status, categoryId, authorId, fromDate, toDate, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Détail d'une annonce",
            description = "Retourne le détail complet d'une annonce par son ID. Endpoint public.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Annonce trouvée"),
                    @ApiResponse(responseCode = "404", description = "Annonce introuvable",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<AnnonceResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(annonceService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Créer une annonce",
            description = "Crée une annonce en statut DRAFT. L'auteur est l'utilisateur authentifié. Nécessite un token JWT.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Exemple création",
                            value = """
                                    {
                                        "title": "Appartement T3 lumineux",
                                        "description": "Bel appartement au centre-ville",
                                        "adress": "12 rue de la Paix, Paris",
                                        "mail": "contact@example.com",
                                        "categoryId": 1
                                    }
                                    """
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Annonce créée"),
                    @ApiResponse(responseCode = "400", description = "Requête invalide",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Non authentifié")
            }
    )
    public ResponseEntity<AnnonceResponseDto> create(@Valid @RequestBody AnnonceRequestDto request) {
        AnnonceResponseDto created = annonceService.create(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Modifier une annonce",
            description = "Mise à jour complète. Seul l'auteur (ou un ADMIN) peut modifier. Interdit si PUBLISHED.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Annonce modifiée"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé (pas l'auteur)",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Annonce introuvable",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Conflit (PUBLISHED ou version)",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<AnnonceResponseDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody AnnonceRequestDto request) {
        return ResponseEntity.ok(annonceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Supprimer une annonce",
            description = "Supprime une annonce. Elle doit être ARCHIVED au préalable. Seul l'auteur (ou un ADMIN) peut supprimer.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Annonce supprimée"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Accès refusé",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Annonce introuvable",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Annonce non archivée",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        annonceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Changer le statut d'une annonce",
            description = "Seul un ADMIN peut archiver. L'auteur peut publier. Transitions interdites : ARCHIVED → PUBLISHED.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Exemple changement de statut",
                            value = """
                                    {"status": "PUBLISHED"}
                                    """
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statut modifié"),
                    @ApiResponse(responseCode = "401", description = "Non authentifié"),
                    @ApiResponse(responseCode = "403", description = "Rôle insuffisant (archivage = ADMIN only)",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Transition de statut invalide",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
            }
    )
    public ResponseEntity<AnnonceResponseDto> patchStatus(@PathVariable Long id,
                                                          @Valid @RequestBody StatusPatchDto request) {
        return ResponseEntity.ok(annonceService.changeStatus(id, request));
    }

    @GetMapping("/meta/filterable-fields")
    @Operation(
            summary = "Champs filtrables (introspection)",
            description = "Retourne la liste des champs filtrables/recherchables détectés dynamiquement par réflexion sur l'entité Annonce."
    )
    public ResponseEntity<List<String>> getFilterableFields() {
        return ResponseEntity.ok(AnnonceSpecification.getFilterableFields());
    }
}
