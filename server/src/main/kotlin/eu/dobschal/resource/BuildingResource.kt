package eu.dobschal.resource

import eu.dobschal.model.dto.request.BaseCoordinatesDto
import eu.dobschal.model.dto.request.CreateBuildingRequestDto
import eu.dobschal.model.dto.request.LevelUpBuildingRequestDto
import eu.dobschal.model.dto.request.SellBeerRequest
import eu.dobschal.model.dto.response.CollectBeerRequestDto
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
    fun createStartVillage(request: BaseCoordinatesDto) = buildingService.saveStartVillage(request.x, request.y)

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/create-city")
    fun createCity(request: CreateBuildingRequestDto) =
        buildingService.createCity(request.x, request.y, request.type)

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/level-up")
    fun upgradeBuilding(request: LevelUpBuildingRequestDto) =
        buildingService.levelUpBuilding(request.buildingId)

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/")
    fun createBuilding(request: CreateBuildingRequestDto) =
        buildingService.createBuilding(request.x, request.y, request.type)

    @RolesAllowed(USER_ROLE)
    @DELETE
    @Path("/")
    fun destroyBuilding(request: BaseCoordinatesDto) =
        buildingService.destroyBuilding(request.x, request.y)


    @RolesAllowed(USER_ROLE)
    @GET
    fun getBuildings(
        @QueryParam("x1") x1: Int,
        @QueryParam("x2") x2: Int,
        @QueryParam("y1") y1: Int,
        @QueryParam("y2") y2: Int
    ) = buildingService.getBuildings(x1, x2, y1, y2)

    @RolesAllowed(USER_ROLE)
    @GET
    @Path("/by-user")
    fun getUsersBuildings(
        @QueryParam("user_id") userId: Int
    ) = buildingService.getUsersBuildings(userId)

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/collect-beer")
    fun collectBeer(request: CollectBeerRequestDto) =
        buildingService.collectBeer(request.buildingId, request.amountOfBeer)

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/sell-beer")
    fun sellBeer(request: SellBeerRequest) =
        buildingService.sellBeer(request.amountOfBeer)

}
