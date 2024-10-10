package eu.dobschal.resource

import io.smallrye.jwt.build.Jwt
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
class UserResource {

    @GET
    fun login(): Response {

        val token = Jwt.issuer("https://castles.dobschal.eu")
            .upn("dobschal")
            .groups(setOf("user"))
            .sign()

        return Response
            .ok(token)
            .build()
    }

}
