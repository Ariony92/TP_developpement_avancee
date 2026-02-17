package tp_avancee_dev.tp_avancee.api.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import tp_avancee_dev.tp_avancee.api.exceptions.BusinessConflictException;

import java.util.List;

@Provider
public class BusinessConflictExceptionMapper implements ExceptionMapper<BusinessConflictException> {

    @Override
    public Response toResponse(BusinessConflictException exception) {
        ApiErrorResponse payload = new ApiErrorResponse("BUSINESS_CONFLICT", List.of(exception.getMessage()));

        return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON).entity(payload).build();
    }
}
