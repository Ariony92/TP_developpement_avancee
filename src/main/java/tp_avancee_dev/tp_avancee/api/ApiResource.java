package tp_avancee_dev.tp_avancee.api;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import tp_avancee_dev.tp_avancee.api.exceptions.BusinessConflictException;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ApiResource {

    @GET
    @Path("helloWorld")
    public Map<String, String> helloWorld() {
        return Map.of("message", "Hello World");
    }

    @GET
    @Path("params")
    public Map<String, Object> queryParams(
            @QueryParam("name") @DefaultValue("anonymous") String name,
            @QueryParam("age") @DefaultValue("0") int age) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "query");
        payload.put("name", name);
        payload.put("age", age);
        payload.put("message", "Bonjour " + name + ", age=" + age);
        return payload;
    }

    @GET
    @Path("params/{id}")
    public Map<String, Object> pathParams(@PathParam("id") long id) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "path");
        payload.put("id", id);
        payload.put("message", "Parametre de chemin recu: " + id);
        return payload;
    }

    @GET
    @Path("errors/{code}")
    public Map<String, String> simulateError(@PathParam("code") int code) {
        if (code == 400) {
            throw new BadRequestException("Erreur de validation simulée");
        }
        if (code == 404) {
            throw new NotFoundException("Ressource simulée introuvable");
        }
        if (code == 409) {
            throw new BusinessConflictException("Conflit métier simulé");
        }
        if (code == 500) {
            throw new RuntimeException("Erreur interne simulée");
        }

        return Map.of("message", "Codes supportés: 400, 404, 409, 500");
    }
}
