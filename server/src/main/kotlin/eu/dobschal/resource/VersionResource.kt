package eu.dobschal.resource

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.config.inject.ConfigProperty

@Path("/v1/version")
@Produces(MediaType.APPLICATION_JSON)
class VersionResource {

    @ConfigProperty(name = "quarkus.application.version")
    lateinit var version: String

    @GET
    @RolesAllowed("user")
    fun apiVersion(): Response {
        return Response
            .ok(version)
            .build()
    }
}
