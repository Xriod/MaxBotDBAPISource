package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import ru.wrappers.GenericResponseWrapper;

@Provider
@Singleton
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {
    private final Logger logger = Logger.getLogger(PersistenceExceptionMapper.class);
    //500
    @Override
    public Response toResponse(PersistenceException e) {
        logger.error("Ошибка базы данных",e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(GenericResponseWrapper.sqlServerException(e.getMessage()))
                .build();
    }
}
