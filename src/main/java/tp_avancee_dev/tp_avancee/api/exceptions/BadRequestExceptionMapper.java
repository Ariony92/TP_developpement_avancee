package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(BadRequestException exception) {
        String message = exception.getMessage() == null ? "Bad request" : exception.getMessage();
        ApiErrorResponse payload = new ApiErrorResponse("BAD_REQUEST", List.of(message));

        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
