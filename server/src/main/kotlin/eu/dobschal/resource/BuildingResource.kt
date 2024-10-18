package eu.dobschal.resource

import eu.dobschal.model.dto.request.SaveStartVillageRequestDto
import eu.dobschal.service.BuildingService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType

@Path("/v1/buildings")
@Produces(MediaType.APPLICATION_JSON)
class BuildingResource @Inject constructor(private val buildingService: BuildingService) {

    @RolesAllowed(USER_ROLE)
    @GET
    @Path("/start-village")
    fun getStartVillage() = buildingService.getStartVillage()

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/start-village")
    fun register(request: SaveStartVillageRequestDto) = buildingService.saveStartVillage(request.x, request.y)


}
