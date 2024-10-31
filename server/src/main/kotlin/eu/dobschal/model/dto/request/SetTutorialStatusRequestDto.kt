package eu.dobschal.model.dto.request

import eu.dobschal.model.enum.TutorialStatus

data class SetTutorialStatusRequestDto(
    val status: TutorialStatus,
    val tutorialId: Int
)
