package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.wrappers.GenericResponseWrapper;

@Provider
@Singleton
public class AlreadyExistExceptionMapper implements ExceptionMapper<AlreadyExistException> {

    //409
    @Override
    public Response toResponse(AlreadyExistException e) {
        return Response.status(Response.Status.CONFLICT)
                .entity(GenericResponseWrapper.customResponse(e.getMessage()))
                .build();
    }
}
