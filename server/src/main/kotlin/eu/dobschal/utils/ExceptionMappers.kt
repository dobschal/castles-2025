package eu.dobschal.utils

import eu.dobschal.model.dto.response.ErrorResponseDto
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider

@Provider
class BadRequestExceptionMapper : ExceptionMapper<BadRequestException> {
    override fun toResponse(exception: BadRequestException): Response {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(ErrorResponseDto(exception.message))
            .build()
    }
}

@Provider
class NotFoundExceptionMapper : ExceptionMapper<NotFoundException> {
    override fun toResponse(exception: NotFoundException): Response {
        return Response
            .status(Response.Status.NOT_FOUND)
            .entity(ErrorResponseDto(exception.message))
            .build()
    }
}


