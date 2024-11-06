package eu.dobschal.resource

import eu.dobschal.service.EventService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType


@Path("/v1/events")
@Produces(MediaType.APPLICATION_JSON)
class EventResource @Inject constructor(private val eventService: EventService) {

    @RolesAllowed(USER_ROLE)
    @GET
    fun getEvents(
        @QueryParam("x1") x1: Int,
        @QueryParam("x2") x2: Int,
        @QueryParam("y1") y1: Int,
        @QueryParam("y2") y2: Int,
        @QueryParam("last_event_id") lastEventId: Int?
    ) = eventService.getEventsBetween(x1, x2, y1, y2, lastEventId)
}
