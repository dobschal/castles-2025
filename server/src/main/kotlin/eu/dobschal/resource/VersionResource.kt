package eu.dobschal.resource

import eu.dobschal.model.dto.VersionResponseDto
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.config.inject.ConfigProperty

@Path("/v1/version")
@Produces(MediaType.APPLICATION_JSON)
class VersionResource {

    @ConfigProperty(name = "quarkus.application.version")
    lateinit var version: String

    @GET
    fun apiVersion(): VersionResponseDto {
        return VersionResponseDto(version)
    }
}
