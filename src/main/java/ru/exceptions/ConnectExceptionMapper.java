package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import ru.wrappers.GenericResponseWrapper;

import java.net.ConnectException;

@Provider
@Singleton
public class ConnectExceptionMapper implements ExceptionMapper<ConnectException> {
    private final Logger logger = Logger.getLogger(ConnectExceptionMapper.class);
    //503
    @Override
    public Response toResponse(ConnectException e) {
        logger.error("Ошибка подключения к базе данных",e);
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(GenericResponseWrapper.sqlServerException("Unable to connect to database"))
                .build();
    }
}
