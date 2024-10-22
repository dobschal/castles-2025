package eu.dobschal.resource

import eu.dobschal.model.dto.request.SaveStartVillageRequestDto
import eu.dobschal.service.BuildingService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.*
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
    fun createStartVillage(request: SaveStartVillageRequestDto) = buildingService.saveStartVillage(request.x, request.y)

    @RolesAllowed(USER_ROLE)
    @GET
    fun getBuildings(
        @QueryParam("x1") x1: Int,
        @QueryParam("x2") x2: Int,
        @QueryParam("y1") y1: Int,
        @QueryParam("y2") y2: Int
    ) = buildingService.getBuildings(x1, x2, y1, y2)
}
