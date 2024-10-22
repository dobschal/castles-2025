package eu.dobschal.service

import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.UnitRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException

@ApplicationScoped
class UnitService @Inject constructor(
    private val unitRepository: UnitRepository,
    private val userService: UserService
) {

    fun getUnits(x1: Int, x2: Int, y1: Int, y2: Int) = unitRepository.findUnitsBetween(x1, x2, y1, y2)

    fun createUnit(x: Int, y: Int, type: UnitType): Unit {

        // TODO: check if user has enough beer, deduct beer from user

        // TODO: check if user has correct building to create unit

        // TODO: add event for new unit

        unitRepository.findUnitByXAndY(x, y)?.let {
            throw BadRequestException("Unit already exists on this map tile")
        }

        val user = userService.getCurrentUser()
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
