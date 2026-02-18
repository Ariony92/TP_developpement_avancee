package tp_avancee_dev.tp_avancee;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import tp_avancee_dev.tp_avancee.api.exceptions.ApiErrorResponse;
import tp_avancee_dev.tp_avancee.api.exceptions.BusinessConflictExceptionMapper;
import tp_avancee_dev.tp_avancee.api.exceptions.ConstraintViolationExceptionMapper;
import tp_avancee_dev.tp_avancee.api.exceptions.NotAuthorizedExceptionMapper;
import tp_avancee_dev.tp_avancee.api.exceptions.BusinessConflictException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiErrorMappersUnitTest {

    @Test
    void notAuthorizedMapper_shouldReturn401WithNormalizedPayload() {
        NotAuthorizedExceptionMapper mapper = new NotAuthorizedExceptionMapper();

        Response response = mapper.toResponse(new NotAuthorizedException("Token manquant"));

        assertEquals(401, response.getStatus());
        ApiErrorResponse payload = (ApiErrorResponse) response.getEntity();
        assertEquals("UNAUTHORIZED", payload.getError());
        assertEquals("Token manquant", payload.getMessages().get(0));
    }

    @Test
    void businessConflictMapper_shouldReturn409WithNormalizedPayload() {
        BusinessConflictExceptionMapper mapper = new BusinessConflictExceptionMapper();

        Response response = mapper.toResponse(new BusinessConflictException("Conflit métier"));

        assertEquals(409, response.getStatus());
        ApiErrorResponse payload = (ApiErrorResponse) response.getEntity();
        assertEquals("BUSINESS_CONFLICT", payload.getError());
        assertEquals("Conflit métier", payload.getMessages().get(0));
    }

    @Test
    void constraintViolationMapper_shouldReturn400AndValidationError() {
        ConstraintViolationExceptionMapper mapper = new ConstraintViolationExceptionMapper();

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(propertyPath.toString()).thenReturn("title");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("is required");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        Response response = mapper.toResponse(exception);

        assertEquals(400, response.getStatus());
        ApiErrorResponse payload = (ApiErrorResponse) response.getEntity();
        assertEquals("VALIDATION_ERROR", payload.getError());
        assertTrue(payload.getMessages().get(0).contains("title"));
        assertTrue(payload.getMessages().get(0).contains("is required"));
    }
}
