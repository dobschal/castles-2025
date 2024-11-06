package eu.dobschal.resource

import eu.dobschal.model.dto.request.CreateUnitRequestDto
import eu.dobschal.model.dto.request.MoveUnitRequestDto
import eu.dobschal.service.UnitService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType

@Path("/v1/units")
@Produces(MediaType.APPLICATION_JSON)
class UnitResource @Inject constructor(private val unitService: UnitService) {

    @RolesAllowed(USER_ROLE)
    @GET
    fun getUnits(
        @QueryParam("x1") x1: Int,
        @QueryParam("x2") x2: Int,
        @QueryParam("y1") y1: Int,
        @QueryParam("y2") y2: Int
    ) = unitService.getUnits(x1, x2, y1, y2)

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/")
    fun createUnit(request: CreateUnitRequestDto) = unitService.createUnit(request.x, request.y, request.type)

    @RolesAllowed(USER_ROLE)
    @POST
    @Path("/move")
    fun moveUnit(request: MoveUnitRequestDto) = unitService.moveUnit(request.x, request.y, request.unitId)

    @RolesAllowed(USER_ROLE)
    @GET
    @Path("/by-user")
    fun getUnits(
        @QueryParam("user_id") userId: Int
    ) = unitService.getUsersUnits(userId)
}
