package eu.dobschal.service

import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.UnitRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException

@ApplicationScoped
class UnitService @Inject constructor(
    private val buildingRepository: BuildingRepository,
    private val unitRepository: UnitRepository,
    private val userService: UserService
) {

    val buildingUnitMapping: Map<UnitType, BuildingType> = mapOf(
        UnitType.WORKER to BuildingType.VILLAGE,
        UnitType.HORSEMAN to BuildingType.CASTLE,
        UnitType.SPEARMAN to BuildingType.CASTLE,
        UnitType.SWORDSMAN to BuildingType.CASTLE
    )

    fun getUnits(x1: Int, x2: Int, y1: Int, y2: Int) = unitRepository.findUnitsBetween(x1, x2, y1, y2)

    fun createUnit(x: Int, y: Int, type: UnitType): Unit {
        val neededBuildingType = buildingUnitMapping[type]
        val user = userService.getCurrentUser()

        buildingRepository.findBuildingByXAndY(x, y)?.let {
            if (it.type != neededBuildingType) {
                throw BadRequestException("serverError.wrongBuildingType")
            }
            if (it.user?.id != user.id) {
                throw BadRequestException("serverError.wrongBuildingOwner")
            }
        } ?: throw BadRequestException("serverError.noBuilding")


        unitRepository.findUnitByXAndY(x, y)?.let {
            throw BadRequestException("serverError.conflictingUnit")
        }

        val unit = Unit().apply {
            this.x = x
            this.y = y
            this.type = type
            this.user = user
        }
        unitRepository.save(unit)
        return unit
    }
}
