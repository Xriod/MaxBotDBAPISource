package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.wrappers.GenericResponseWrapper;

@Provider
@Singleton
public class NumberFormatExceptionMapper implements ExceptionMapper<NumberFormatException> {
    //400
    @Override
    public Response toResponse(NumberFormatException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(GenericResponseWrapper.customResponse(e.getMessage()))
                .build();
    }
}
