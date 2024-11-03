package eu.dobschal.service

import eu.dobschal.model.dto.response.UnitsResponseDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.Event
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
import eu.dobschal.utils.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import kotlin.math.abs
import kotlin.math.max

@ApplicationScoped
class UnitService @Inject constructor(
    private val mapTileRepository: MapTileRepository,
    private val buildingRepository: BuildingRepository,
    private val unitRepository: UnitRepository,
    private val userService: UserService,
    private val eventRepository: EventRepository,
    private val priceService: PriceService,
    private val userRepository: UserRepository
) {

    val buildingUnitMapping: Map<UnitType, BuildingType> = mapOf(
        UnitType.WORKER to BuildingType.VILLAGE,
        UnitType.HORSEMAN to BuildingType.CASTLE,
        UnitType.SPEARMAN to BuildingType.CASTLE,
        UnitType.SWORDSMAN to BuildingType.CASTLE
    )

    fun getUnits(x1: Int, x2: Int, y1: Int, y2: Int): UnitsResponseDto {
        return UnitsResponseDto(unitRepository.findUnitsBetween(x1, x2, y1, y2))
    }

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

        val price = priceService.getPriceForUnitCreation(user.toDto(), type)
        if (user.beer!! < price) {
            throw BadRequestException("serverError.notEnoughBeer")
        }

        buildingRepository.countCastlesByUser(user.id!!).let {
            if (max(2, it * UNITS_PER_CASTLE_LEVEL_1) <= unitRepository.countUnitsByUser(user.id!!)) {
                throw BadRequestException("serverError.tooManyUnits")
            }
        }

        val unit = Unit().apply {
            this.x = x
            this.y = y
            this.type = type
            this.user = user
        }
        unitRepository.save(unit)
        userRepository.deductBeerFromUser(user.id!!, price)
        eventRepository.save(Event().apply {
            this.user1 = user
            this.type = EventType.UNIT_CREATED
            this.unit = unit
            this.x = x
            this.y = y
        })
        return unit
    }

    fun moveUnit(x: Int, y: Int, unitId: Int): Unit {
        val user = userService.getCurrentUser()
        val unit = unitRepository.findById(unitId) ?: throw NotFoundException("serverError.noUnit")
        if (unit.user?.id != user.id) {
            throw BadRequestException("serverError.wrongUnitOwner")
        }
        val (conflictingBuilding, conflictingUnit) = getMoveConflictingBuildingsAndUnits(x, y, unit, user)
        val isDistanceWrong = abs(unit.x!! - x) > 1 || abs(unit.y!! - y) > 1 || (unit.y == y && unit.x == x)
        if (isDistanceWrong) {
            throw BadRequestException("serverError.wrongDistance")
        }
        val isMapTileWater = mapTileRepository.findByXAndY(x, y)?.type == MapTileType.WATER
        if (isMapTileWater) {
            throw BadRequestException("serverError.cannotMoveOnWaterTile")
        }
        val price = priceService.getPriceForUnitMove(unit.type)
        if (user.beer!! < price) {
            throw BadRequestException("serverError.notEnoughBeer")
        }
        eventRepository.countEventsByUnitIdAndTypeLastHour(unit.id!!, EventType.UNIT_MOVED).let {
            val maxMovesPerHours = when (unit.type) {
                UnitType.WORKER -> WORKER_MOVES_PER_HOUR
                UnitType.HORSEMAN -> HORSEMAN_MOVES_PER_HOUR
                UnitType.SPEARMAN -> SPEARMAN_MOVES_PER_HOUR
                UnitType.SWORDSMAN -> SWORDSMAN_MOVES_PER_HOUR
            }
            if (it >= maxMovesPerHours) {
                throw BadRequestException("serverError.tooManyMoves")
            }
        }
        unit.x = x
        unit.y = y
        unitRepository.updatePosition(unit.id!!, x, y)
        userRepository.deductBeerFromUser(user.id!!, price)
        eventRepository.save(Event().apply {
            this.user1 = user
            this.type = EventType.UNIT_MOVED
            this.unit = unit
            this.x = x
            this.y = y
        })
        handleFightAndConquer(conflictingUnit, user, unit, conflictingBuilding)
        return unit
    }

    fun handleFightAndConquer(
        conflictingUnit: Unit?,
        user: User,
        unit: Unit,
        conflictingBuilding: Building?
    ): Boolean {
        if (conflictingUnit != null && conflictingUnit.user?.id != user.id) {
            val lostFight = handleFight(conflictingUnit, unit) != unit
            if (lostFight) {
                return true
            }
        }
        if (conflictingBuilding != null && conflictingBuilding.user?.id != user.id) {
            handleConquerBuilding(conflictingBuilding, unit, user)
        }
        return false
    }

    fun getMoveConflictingBuildingsAndUnits(
        x: Int,
        y: Int,
        unit: Unit,
        user: User
    ): Pair<Building?, Unit?> {
        val conflictingBuilding = buildingRepository.findBuildingByXAndY(x, y)?.let { building ->
            if (unit.type == UnitType.WORKER && building.user?.id != user.id) {
                throw BadRequestException("serverError.wrongBuildingOwner")
            }
            building
        }
        val conflictingUnit = unitRepository.findUnitByXAndY(x, y)?.let {
            if (it.user?.id == user.id || unit.type == UnitType.WORKER) {
                throw BadRequestException("serverError.conflictingUnit")
            }
            it
        }
        return Pair(conflictingBuilding, conflictingUnit)
    }

    private fun handleConquerBuilding(conflictingBuilding: Building, unit: Unit, user: User) {
        if (conflictingBuilding.type == BuildingType.VILLAGE || conflictingBuilding.type == BuildingType.CASTLE) {
            buildingRepository.updateOwner(conflictingBuilding.id!!, user.id!!)
            eventRepository.save(Event().apply {
                this.user1 = user
                this.type = EventType.BUILDING_CONQUERED
                this.building = conflictingBuilding
                this.x = conflictingBuilding.x!!
                this.y = conflictingBuilding.y!!
            })
            // TODO: if was last village or castle --> game over
        }
        if (conflictingBuilding.type == BuildingType.FARM || conflictingBuilding.type == BuildingType.BREWERY) {
            buildingRepository.delete(conflictingBuilding)
            eventRepository.save(Event().apply {
                this.user1 = user
                this.type = EventType.BUILDING_DESTROYED
                this.x = conflictingBuilding.x!!
                this.y = conflictingBuilding.y!!
            })
        }
    }

    private fun handleFight(unit1: Unit, unit2: Unit): Unit {
        val looserUnit = when {
            unit1.type == unit2.type -> if (flipCoin()) unit2 else unit1
            unit1.type == UnitType.WORKER -> unit1
            unit2.type == UnitType.WORKER -> unit2
            else -> determineLooser(unit1, unit2)
        }
        val winnerUnit = if (looserUnit == unit1) {
            unit2
        } else {
            unit1
        }
        eventRepository.save(Event().apply {
            this.user1 = looserUnit.user
            this.user2 = winnerUnit.user
            this.type = EventType.LOST_UNIT
            this.x = unit1.x!!
            this.y = unit1.y!!
        })
        unitRepository.deleteById(looserUnit.id!!)
        return winnerUnit
    }

    private fun determineLooser(unit1: Unit, unit2: Unit): Unit {
        return when {
            unit1.type == UnitType.HORSEMAN && unit2.type == UnitType.SPEARMAN -> unit1 // Spearmen wins against Horseman
            unit1.type == UnitType.SPEARMAN && unit2.type == UnitType.SWORDSMAN -> unit1 // Swordsmen wins against Spearmen
            unit1.type == UnitType.SWORDSMAN && unit2.type == UnitType.HORSEMAN -> unit1 // Horseman wins against Swordsmen
            else -> unit2
        }
    }
}
