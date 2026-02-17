package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        String message = exception.getMessage() == null ? "Invalid argument" : exception.getMessage();
        Response.Status status = message.toLowerCase().contains("introuvable")
                ? Response.Status.NOT_FOUND : Response.Status.BAD_REQUEST;

        ApiErrorResponse payload = new ApiErrorResponse(status == Response.Status.NOT_FOUND ? "NOT_FOUND" : "BUSINESS_ERROR", List.of(message));

        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
