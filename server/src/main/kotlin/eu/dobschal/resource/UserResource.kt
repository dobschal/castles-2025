package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.service.UserService
import io.smallrye.jwt.build.Jwt
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
class UserResource @Inject constructor(private val userService: UserService) {

    @POST
    @Path("/login")
    fun login(request: UserCredentialsDto): Response {

        val token = Jwt.issuer("https://castles.dobschal.eu")
            .upn("dobschal")
            .groups(setOf("user"))
            .sign()

        return Response
            .ok(token)
            .build()
    }

    @POST
    @Path("/register")
    fun register(request: UserCredentialsDto) = userService.registerUser(request.username, request.password)

}
