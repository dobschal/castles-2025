package eu.dobschal.resource

import eu.dobschal.model.dto.request.SetTutorialStatusRequestDto
import eu.dobschal.service.TutorialService
import eu.dobschal.utils.USER_ROLE
import jakarta.annotation.security.RolesAllowed
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType


@Path("/v1/tutorials")
@Produces(MediaType.APPLICATION_JSON)
class TutorialResource @Inject constructor(private val tutorialService: TutorialService) {

    @RolesAllowed(USER_ROLE)
    @GET
    @Path("/next")
    fun getNextTutorial(
    ) = tutorialService.getNextTutorial()

    @RolesAllowed(USER_ROLE)
    @POST
    fun setTutorialStatus(request: SetTutorialStatusRequestDto) =
        tutorialService.setTutorialStatus(request.tutorialId, request.status)
}
