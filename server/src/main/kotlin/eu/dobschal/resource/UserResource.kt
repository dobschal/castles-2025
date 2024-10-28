package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.service.UserService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
class UserResource @Inject constructor(private val userService: UserService) {

    @POST
    @Path("/login")
    @PermitAll
    fun login(request: UserCredentialsDto) = userService.loginUser(request.username, request.password)

    @POST
    @Path("/register")
    @PermitAll
    fun register(request: UserCredentialsDto) = userService.registerUser(request.username, request.password)

    @GET
    @Path("/current")
    @RolesAllowed(USER_ROLE)
    fun getCurrentUser() = userService.getCurrentUserDto()

}
