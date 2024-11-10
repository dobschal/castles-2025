package eu.dobschal.service

import eu.dobschal.model.entity.Tutorial
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.*
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.EventRepository
import eu.dobschal.repository.TutorialRepository
import eu.dobschal.repository.UnitRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class TutorialService @Inject constructor(
    private val tutorialRepository: TutorialRepository,
    private val userService: UserService,
    private val buildingRepository: BuildingRepository,
    private val unitRepository: UnitRepository,
    private val eventRepository: EventRepository
) {

    val tutorialTypesOrdered = listOf(
        TutorialType.FIRST_WORKER,
        TutorialType.FIRST_FARM,
        TutorialType.FIRST_BREWERY,
        TutorialType.FIRST_BEER_COLLECTED,
        TutorialType.FIRST_CASTLE,
        TutorialType.FIRST_UNIT
    )

    fun setTutorialStatus(tutorialId: Int, status: TutorialStatus) = tutorialRepository.setStatus(tutorialId, status)

    fun getNextTutorial(): Tutorial? {
        val currentUser = userService.getCurrentUser()
        tutorialRepository.findLatestTutorial(currentUser.id!!)?.let { latestTutorial ->
            if (latestTutorial.status == TutorialStatus.OPEN || latestTutorial.status == TutorialStatus.CAN_BE_COMPLETED) {
                if (latestTutorial.status == TutorialStatus.OPEN && checkIfTutorialCanBeCompleted(
                        latestTutorial.type,
                        currentUser
                    )
                ) {
                    return latestTutorial.apply {
                        status = TutorialStatus.CAN_BE_COMPLETED
                    }
                }
                return latestTutorial
            }
            val nextTutorialType = tutorialTypesOrdered.getOrNull(tutorialTypesOrdered.indexOf(latestTutorial.type) + 1)
            if (nextTutorialType != null) {
                return tutorialRepository.save(Tutorial().apply {
                    user = currentUser
                    type = nextTutorialType
                    status = if (checkIfTutorialCanBeCompleted(
                            nextTutorialType,
                            currentUser
                        )
                    ) TutorialStatus.CAN_BE_COMPLETED else TutorialStatus.OPEN
                })
            }
            throw NotFoundException("serverError.noMoreTutorials")
        } ?: run {
            return tutorialRepository.save(Tutorial().apply {
                user = currentUser
                type = tutorialTypesOrdered.first()
                status = if (checkIfTutorialCanBeCompleted(
                        tutorialTypesOrdered.first(),
                        currentUser
                    )
                ) TutorialStatus.CAN_BE_COMPLETED else TutorialStatus.OPEN
            })
        }
    }

    fun checkIfTutorialCanBeCompleted(tutorialType: TutorialType, user: User): Boolean {
        return when (tutorialType) {
            TutorialType.FIRST_WORKER -> unitRepository.findUnitByTypeAndUser(
                user.id!!,
                UnitType.WORKER
            ) != null || buildingRepository.findBuildingByUserAndType(
                user.id!!,
                BuildingType.FARM
            ) != null // If the user has a farm, he had a worker too

            TutorialType.FIRST_FARM -> buildingRepository.findBuildingByUserAndType(
                user.id!!,
                BuildingType.FARM
            ) != null

            TutorialType.FIRST_BREWERY -> buildingRepository.findBuildingByUserAndType(
                user.id!!,
                BuildingType.BREWERY
            ) != null

            TutorialType.FIRST_BEER_COLLECTED -> eventRepository.findEventByTypeAndUser(
                user.id!!,
                EventType.BEER_COLLECTED
            ) != null

            TutorialType.FIRST_CASTLE -> buildingRepository.findBuildingByUserAndType(
                user.id!!,
                BuildingType.CASTLE
            ) != null

            TutorialType.FIRST_UNIT -> unitRepository.findUnitByTypeInAndUser(
                user.id!!,
                listOf(UnitType.SPEARMAN, UnitType.HORSEMAN, UnitType.SWORDSMAN)
            ) != null
        }
    }

}
