package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;

@Provider
public class NotAuthorizedExceptionMapper implements ExceptionMapper<NotAuthorizedException> {

    @Override
    public Response toResponse(NotAuthorizedException exception) {
        String message = exception.getMessage() == null ? "Unauthorized" : exception.getMessage();
        ApiErrorResponse payload = new ApiErrorResponse("UNAUTHORIZED", List.of(message));

        return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
