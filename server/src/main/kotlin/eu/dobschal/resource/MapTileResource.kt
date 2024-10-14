package eu.dobschal.resource

import eu.dobschal.service.MapTileService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType

@Path("/v1/map-tiles")
@Produces(MediaType.APPLICATION_JSON)
class MapTileResource @Inject constructor(private val mapTileService: MapTileService) {

    @RolesAllowed(USER_ROLE)
    @GET
    fun getMapTiles(
        @QueryParam("x1") x1: Int,
        @QueryParam("x2") x2: Int,
        @QueryParam("y1") y1: Int,
        @QueryParam("y2") y2: Int
    ) = mapTileService.getMapTiles(x1, x2, y1, y2)

}
