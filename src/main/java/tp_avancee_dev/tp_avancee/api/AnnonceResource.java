package tp_avancee_dev.tp_avancee.api;

import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import tp_avancee_dev.tp_avancee.api.dto.AnnonceRequestDto;
import tp_avancee_dev.tp_avancee.api.dto.AnnonceResponseDto;
import tp_avancee_dev.tp_avancee.api.dto.PagedResponseDto;
import tp_avancee_dev.tp_avancee.api.dto.StatusPatchDto;
import tp_avancee_dev.tp_avancee.api.mapper.AnnonceMapper;
import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.service.AnnonceService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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
    public Response listAnnonces(@DefaultValue("1") @QueryParam("page") int page, @DefaultValue("10") @QueryParam("size") int size, @QueryParam("keyword") String keyword) {

        List<Annonce> annonces = (keyword == null || keyword.isBlank())
                ? annonceService.listAnnoncesPaginated(page, size)
                : annonceService.searchAnnonces(keyword, page, size);

        List<AnnonceResponseDto> items = annonces.stream().map(AnnonceMapper::toBasicDto)
                .collect(Collectors.toList());

        return Response.ok(new PagedResponseDto<>(Math.max(page, 1), Math.max(size, 1), items.size(), items)).build();
    }

    @GET
    @Path("/{id}")
    public Response getAnnonceById(@PathParam("id") Long id) {
        Annonce annonce = annonceService.getAnnonceById(id);
        if (annonce == null) {
            throw new NotFoundException("Annonce introuvable : id=" + id);
        }
        return Response.ok(AnnonceMapper.toDetailedDto(annonce)).build();
    }

    @POST
    public Response createAnnonce(@Valid AnnonceRequestDto request, @Context UriInfo uriInfo) {
        if (request.getAuthorId() == null) {
            throw new BadRequestException("authorId is required");
        }

        Annonce created = annonceService.createAnnonce(
                request.getTitle(),
                request.getDescription(),
                request.getAdress(),
                request.getMail(),
                request.getAuthorId(),
                request.getCategoryId()
        );

        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build();
        return Response.created(location).entity(AnnonceMapper.toBasicDto(created)).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateAnnonce(@PathParam("id") Long id, @Valid AnnonceRequestDto request) {
        Annonce updated = annonceService.updateAnnonce(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getAdress(),
                request.getMail(),
                request.getCategoryId()
        );
        return Response.ok(AnnonceMapper.toBasicDto(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAnnonce(@PathParam("id") Long id) {
        boolean deleted = annonceService.deleteAnnonce(id);
        if (!deleted) {
            throw new NotFoundException("Annonce introuvable : id=" + id);
        }
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/status")
    public Response patchAnnonceStatus(@PathParam("id") Long id, @Valid StatusPatchDto request) {
        Annonce updated = annonceService.changeStatusTo(id, request.getStatus());
        return Response.ok(AnnonceMapper.toBasicDto(updated)).build();
    }
}
