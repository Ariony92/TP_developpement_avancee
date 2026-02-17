package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> messages = exception.getConstraintViolations().stream().map(this::formatMessage).collect(Collectors.toList());

        ApiErrorResponse payload = new ApiErrorResponse("VALIDATION_ERROR", messages);
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }

    private String formatMessage(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + " " + violation.getMessage();
    }
}
