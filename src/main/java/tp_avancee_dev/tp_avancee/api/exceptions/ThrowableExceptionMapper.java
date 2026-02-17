package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        ApiErrorResponse payload = new ApiErrorResponse("INTERNAL_ERROR", List.of("Une erreur interne est survenue")
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
