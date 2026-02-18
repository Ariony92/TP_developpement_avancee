package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {

    @Override
    public Response toResponse(AccessDeniedException exception) {
        ApiErrorResponse payload = new ApiErrorResponse("FORBIDDEN", List.of(exception.getMessage()));
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
