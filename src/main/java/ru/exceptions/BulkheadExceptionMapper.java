package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.jboss.logging.Logger;
import ru.wrappers.GenericResponseWrapper;

@Provider
@Singleton
public class BulkheadExceptionMapper implements ExceptionMapper<BulkheadException> {

    private final Logger logger = Logger.getLogger(BulkheadExceptionMapper.class);

    //429
    @Override
    public Response toResponse(BulkheadException e) {
        logger.warn("Очередь переполнена",e);
        return Response.status(Response.Status.TOO_MANY_REQUESTS)
                .entity(GenericResponseWrapper.customResponse(""))
                .build();
    }
}
