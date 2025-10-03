package ru.exceptions;

import jakarta.inject.Singleton;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.wrappers.GenericResponseWrapper;

@Provider
@Singleton
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    //404 && 400
    @Override
    public Response toResponse(NotFoundException e) {
        if(e.getMessage().contains("RESTEASY003870")){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(GenericResponseWrapper.customResponse(e.getMessage()))
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(GenericResponseWrapper.customResponse(e.getMessage()))
                    .build();
        }
    }
}
