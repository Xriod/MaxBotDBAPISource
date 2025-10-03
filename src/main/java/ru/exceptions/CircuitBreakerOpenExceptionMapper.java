package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.jboss.logging.Logger;
import ru.wrappers.GenericResponseWrapper;

@Provider
@Singleton
public class CircuitBreakerOpenExceptionMapper implements ExceptionMapper<CircuitBreakerOpenException> {

    private final Logger logger = Logger.getLogger(CircuitBreakerOpenExceptionMapper.class);

    //429
    @Override
    public Response toResponse(CircuitBreakerOpenException e) {
        logger.warn("Слишком много запросов",e);
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .entity(GenericResponseWrapper.customResponse("Service experience high loads. Try again later."))
                .build();
    }

}
