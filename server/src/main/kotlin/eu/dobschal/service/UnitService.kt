package eu.dobschal.service

import eu.dobschal.model.entity.Event
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import kotlin.math.abs

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

        val price = priceService.getPriceForUnitCreation(user, type)
        if (user.beer!! < price) {
            throw BadRequestException("serverError.notEnoughBeer")
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
        unitRepository.findUnitByXAndY(x, y)?.let {
            if (it.user?.id == user.id) {
                throw BadRequestException("serverError.conflictingUnit")
            }
        }
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
        return unit
    }
}
