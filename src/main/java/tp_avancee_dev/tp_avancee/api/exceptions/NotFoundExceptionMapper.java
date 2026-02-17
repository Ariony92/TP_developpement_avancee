package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        String message = exception.getMessage() == null ? "Resource not found" : exception.getMessage();
        ApiErrorResponse payload = new ApiErrorResponse("NOT_FOUND", List.of(message));

        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
