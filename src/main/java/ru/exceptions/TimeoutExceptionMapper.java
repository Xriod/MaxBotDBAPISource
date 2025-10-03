package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import ru.wrappers.GenericResponseWrapper;

import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

@Provider
@Singleton
public class TimeoutExceptionMapper implements ExceptionMapper<TimeoutException> {
    private final Logger logger = Logger.getLogger(TimeoutExceptionMapper.class);
    //408
    @Override
    public Response toResponse(TimeoutException e) {
        logger.warn("Timeout reached",e);
        return Response.status(Response.Status.REQUEST_TIMEOUT)
                .entity(GenericResponseWrapper.customResponse("Request timeout. Try again later."))
                .build();
    }
}
