package eu.dobschal.resource

import eu.dobschal.service.PriceService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType


@Path("/v1/prices")
@Produces(MediaType.APPLICATION_JSON)
class PriceResource @Inject constructor(private val priceService: PriceService) {

    @RolesAllowed(USER_ROLE)
    @GET
    fun getPrices() = priceService.getAllPrices()
}
