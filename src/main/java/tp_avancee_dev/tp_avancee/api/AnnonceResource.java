package tp_avancee_dev.tp_avancee.api;

import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import tp_avancee_dev.tp_avancee.api.dto.AnnonceRequestDto;
import tp_avancee_dev.tp_avancee.api.dto.AnnonceResponseDto;
import tp_avancee_dev.tp_avancee.api.dto.PagedResponseDto;
import tp_avancee_dev.tp_avancee.api.dto.StatusPatchDto;
import tp_avancee_dev.tp_avancee.api.filter.Secured;
import tp_avancee_dev.tp_avancee.api.log.StructuredLogger;
import tp_avancee_dev.tp_avancee.api.mapper.AnnonceMapper;
import tp_avancee_dev.tp_avancee.api.security.UserPrincipal;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.service.AnnonceService;

import javax.security.auth.Subject;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Secured
@Path("/annonces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AnnonceResource {

    private final AnnonceService annonceService;

    public AnnonceResource() {
        this(new AnnonceService());
    }

    public AnnonceResource(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @GET
    public Response listAnnonces(@DefaultValue("1") @QueryParam("page") int page,
                                 @DefaultValue("10") @QueryParam("size") int size,
                                 @QueryParam("keyword") String keyword) {

        List<Annonce> annonces = (keyword == null || keyword.isBlank())
                ? annonceService.listAnnoncesPaginated(page, size)
                : annonceService.searchAnnonces(keyword, page, size);

        List<AnnonceResponseDto> items = annonces.stream()
                .map(AnnonceMapper::toBasicDto)
                .collect(Collectors.toList());

        StructuredLogger.info("api.annonces.list", Map.of("page", page, "size", size, "count", items.size()));
        return Response.ok(new PagedResponseDto<>(Math.max(page, 1), Math.max(size, 1), items.size(), items)).build();
    }

    @GET
    @Path("/{id}")
    public Response getAnnonceById(@PathParam("id") Long id) {
        Annonce annonce = annonceService.getAnnonceById(id);
        if (annonce == null) {
            StructuredLogger.info("api.annonces.detail.not_found", Map.of("annonceId", id));
            throw new NotFoundException("Annonce introuvable : id=" + id);
        }
        return Response.ok(AnnonceMapper.toDetailedDto(annonce)).build();
    }

    @POST
    public Response createAnnonce(@Valid AnnonceRequestDto request,
                                  @Context UriInfo uriInfo,
                                  @Context ContainerRequestContext requestContext) {

        Subject currentSubject = extractSubject(requestContext);
        Long authUserId = extractAuthUserId(currentSubject, requestContext);
        Long authorId = authUserId != null ? authUserId : request.getAuthorId();
        if (authorId == null) {
            throw new BadRequestException("authorId is required");
        }

        Annonce created = annonceService.createAnnonce(
                request.getTitle(),
                request.getDescription(),
                request.getAdress(),
                request.getMail(),
                authorId,
                request.getCategoryId()
        );

        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        StructuredLogger.info("api.annonces.create", Map.of("annonceId", created.getId(), "authorId", authorId));
        return Response.created(location)
                .entity(AnnonceMapper.toBasicDto(created))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateAnnonce(@PathParam("id") Long id,
                                  @Valid AnnonceRequestDto request,
                                  @Context ContainerRequestContext requestContext) {
        Subject currentSubject = extractSubject(requestContext);
        Long authUserId = extractAuthUserId(currentSubject, requestContext);

        Annonce updated = annonceService.updateAnnonce(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getAdress(),
                request.getMail(),
                request.getCategoryId(),
                currentSubject,
                request.getVersion()
        );
        StructuredLogger.info("api.annonces.update", Map.of("annonceId", id, "userId", authUserId));
        return Response.ok(AnnonceMapper.toBasicDto(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAnnonce(@PathParam("id") Long id,
                                  @Context ContainerRequestContext requestContext) {
        Subject currentSubject = extractSubject(requestContext);
        Long authUserId = extractAuthUserId(currentSubject, requestContext);

        boolean deleted = annonceService.deleteAnnonce(id, currentSubject);
        if (!deleted) {
            throw new NotFoundException("Annonce introuvable : id=" + id);
        }
        StructuredLogger.info("api.annonces.delete", Map.of("annonceId", id, "userId", authUserId));
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/status")
    public Response patchAnnonceStatus(@PathParam("id") Long id,
                                       @Valid StatusPatchDto request,
                                       @Context ContainerRequestContext requestContext) {
        Subject currentSubject = extractSubject(requestContext);
        Long authUserId = extractAuthUserId(currentSubject, requestContext);

        Annonce updated = annonceService.changeStatusTo(id, request.getStatus(), currentSubject);
        StructuredLogger.info("api.annonces.status.patch", Map.of("annonceId", id, "userId", authUserId, "status", request.getStatus()));
        return Response.ok(AnnonceMapper.toBasicDto(updated)).build();
    }

    private Subject extractSubject(ContainerRequestContext requestContext) {
        Object value = requestContext.getProperty("subject");
        if (value instanceof Subject) {
            return (Subject) value;
        }
        return null;
    }

    private Long extractAuthUserId(Subject currentSubject, ContainerRequestContext requestContext) {
        if (currentSubject != null) {
            return currentSubject.getPrincipals(UserPrincipal.class)
                    .stream()
                    .findFirst()
                    .map(UserPrincipal::getUserId)
                    .orElse(null);
        }

        Object value = requestContext.getProperty("authUserId");
        if (value instanceof Long) {
            return (Long) value;
        }
        return null;
    }
}
