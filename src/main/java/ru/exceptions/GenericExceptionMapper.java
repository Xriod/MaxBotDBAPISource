package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import ru.wrappers.GenericResponseWrapper;

@Provider
@Singleton
public class GenericExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger logger = Logger.getLogger(GenericExceptionMapper.class);

    //500
    @Override
    public Response toResponse(Exception e) {
        logger.error("Непредвиденная ошибка",e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(GenericResponseWrapper.unknownError(e.getMessage() +" :"+e.getClass()))
                .build();
    }
}
