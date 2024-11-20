package eu.dobschal.service

import eu.dobschal.model.dto.UnitDto
import eu.dobschal.model.dto.response.SuccessResponseDto
import eu.dobschal.model.dto.response.UnitsResponseDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.Event
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.*
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

    val buildingUnitMapping: Map<UnitType, List<BuildingType>> = mapOf(
        UnitType.WORKER to listOf(BuildingType.VILLAGE, BuildingType.CITY),
        UnitType.HORSEMAN to listOf(BuildingType.CASTLE),
        UnitType.SPEARMAN to listOf(BuildingType.CASTLE),
        UnitType.SWORDSMAN to listOf(BuildingType.CASTLE),
        UnitType.DRAGON to listOf(BuildingType.CASTLE),
        UnitType.ARCHER to listOf(BuildingType.CASTLE)
    )

    fun getUnitsLimit(userId: Int): Int {
        val amountOfCastlesLvl1 = buildingRepository.countCastlesByUser(userId, 1)
        val amountOfCastlesLvl2 = buildingRepository.countCastlesByUser(userId, 2)
        return max(2, amountOfCastlesLvl1 * UNITS_PER_CASTLE_LVL_1 + amountOfCastlesLvl2 * UNITS_PER_CASTLE_LVL_2)
    }

    fun getUnits(x1: Int, x2: Int, y1: Int, y2: Int): UnitsResponseDto {
        val unitsCount = unitRepository.countUnitsByUser(userService.getCurrentUser().id!!, listOf(UnitType.WORKER));
        val unitsLimit = getUnitsLimit(userService.getCurrentUser().id!!)
        return UnitsResponseDto(
            units = unitRepository.findUnitsBetween(x1, x2, y1, y2),
            unitsCount = unitsCount,
            unitsLimit = unitsLimit
        )
    }

    fun createUnit(x: Int, y: Int, type: UnitType): Unit {
        val neededBuildingType = buildingUnitMapping[type]
        val user = userService.getCurrentUser()

        buildingRepository.findBuildingByXAndY(x, y)?.let {
            if (neededBuildingType?.contains(it.type) == false) {
                throw BadRequestException("serverError.wrongBuildingType")
            }
            if (it.user?.id != user.id) {
                throw BadRequestException("serverError.wrongBuildingOwner")
            }
            if ((type == UnitType.DRAGON || type == UnitType.ARCHER) && it.level!! < 2) {
                throw BadRequestException("serverError.wrongBuildingLevel")
            }
        } ?: throw BadRequestException("serverError.noBuilding")

        unitRepository.findUnitByXAndY(x, y)?.let {
            throw BadRequestException("serverError.conflictingUnit")
        }

        val price = priceService.getPriceForUnitCreation(user.toDto(), type)
        val currency = priceService.getUnitsCurrency(type)
        if (currency == Currency.BEER && user.beer!! < price) {
            throw BadRequestException("serverError.notEnoughBeer")
        }
        if (currency == Currency.GOLD && user.gold!! < price) {
            throw BadRequestException("serverError.notEnoughGold")
        }

        if (type != UnitType.WORKER) { // Workers are not limited
            if (getUnitsLimit(user.id!!) <= unitRepository.countUnitsByUser(user.id!!, listOf(UnitType.WORKER))) {
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
        if (currency == Currency.BEER) {
            userRepository.deductBeerFromUser(user.id!!, price)
        } else {
            userRepository.deductGoldFromUser(user.id!!, price)
        }
        eventRepository.save(Event().apply {
            this.user1 = user
            this.type = EventType.UNIT_CREATED
            this.unit = unit
            this.x = x
            this.y = y
        })
        return unit
    }

    fun deleteUnit(unitId: Long): SuccessResponseDto {
        val user = userService.getCurrentUser()
        val unit = unitRepository.findById(unitId) ?: throw NotFoundException("serverError.noUnit")
        if (unit.user?.id != user.id) {
            throw BadRequestException("serverError.wrongUnitOwner")
        }
        unitRepository.delete(unit)
        eventRepository.save(Event().apply {
            this.user1 = user
            this.type = EventType.UNIT_DELETED
            this.unit = unit
            this.x = unit.x!!
            this.y = unit.y!!
        })
        return SuccessResponseDto("serverSuccess.unitDeleted")
    }

    fun moveUnit(x: Int, y: Int, unitId: Int): Unit {
        val user = userService.getCurrentUser()
        val unit = unitRepository.findById(unitId) ?: throw NotFoundException("serverError.noUnit")
        if (unit.user?.id != user.id) {
            throw BadRequestException("serverError.wrongUnitOwner")
        }
        val (conflictingBuilding, conflictingUnit) = getMoveConflictingBuildingsAndUnits(x, y, unit, user)
        if (conflictingBuilding != null && conflictingBuilding.user?.id != user.id && (unit.type == UnitType.DRAGON || unit.type == UnitType.WORKER)) {
            throw BadRequestException("serverError.unitCannotConquer")
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
        val currency = priceService.getUnitsCurrency(unit.type)
        if (currency == Currency.GOLD && user.gold!! < price) {
            throw BadRequestException("serverError.notEnoughGold")
        } else if (user.beer!! < price) {
            throw BadRequestException("serverError.notEnoughBeer")
        }
        eventRepository.countEventsByUnitIdAndTypeLastHour(unit.id!!, EventType.UNIT_MOVED).let {
            val maxMovesPerHours = when (unit.type) {
                UnitType.WORKER -> WORKER_MOVES_PER_HOUR
                UnitType.HORSEMAN -> HORSEMAN_MOVES_PER_HOUR
                UnitType.SPEARMAN -> SPEARMAN_MOVES_PER_HOUR
                UnitType.SWORDSMAN -> SWORDSMAN_MOVES_PER_HOUR
                UnitType.ARCHER -> ARCHER_MOVES_PER_HOUR
                UnitType.DRAGON -> DRAGON_MOVES_PER_HOUR
            }
            if (it >= maxMovesPerHours) {
                throw BadRequestException("serverError.tooManyMoves")
            }
        }
        unit.x = x
        unit.y = y
        unitRepository.updatePosition(unit.id!!, x, y)
        if (currency == Currency.GOLD) {
            userRepository.deductGoldFromUser(user.id!!, price)
        } else {
            userRepository.deductBeerFromUser(user.id!!, price)
        }
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
            val lostFight = handleFight(conflictingUnit, unit, conflictingBuilding) != unit
            if (lostFight) {
                return true
            }
        }
        if (conflictingBuilding != null && conflictingBuilding.user?.id != user.id) {
            handleConquerBuilding(conflictingBuilding, user)
        }
        return false
    }

    // Returns true if lost fight (attack) because of castle defense bonus
    fun checkCastleDefense(conflictingBuilding: Building): Boolean {
        if (conflictingBuilding.type != BuildingType.CASTLE) {
            return false;
        }
        if (conflictingBuilding.level == 1) {
            if (flipCoin() && flipCoin()) { // ~ 25% chance to lose
                return true;
            }
        } else if (conflictingBuilding.level == 2) {
            if (flipCoin()) { // 50% chance to lose
                return true;
            }
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

    private fun handleConquerBuilding(conflictingBuilding: Building, user: User) {
        if (conflictingBuilding.type == BuildingType.VILLAGE || conflictingBuilding.type == BuildingType.CASTLE || conflictingBuilding.type == BuildingType.CITY) {
            val oldUserId = conflictingBuilding.user!!
            buildingRepository.updateOwner(conflictingBuilding.id!!, user.id!!)
            eventRepository.save(Event().apply {
                this.user1 = user
                this.type = EventType.BUILDING_CONQUERED
                this.building = conflictingBuilding
                this.x = conflictingBuilding.x!!
                this.y = conflictingBuilding.y!!
            })
            handleGameOver(oldUserId)
        } else {
            buildingRepository.delete(conflictingBuilding)
            eventRepository.save(Event().apply {
                this.user1 = user
                this.type = EventType.BUILDING_DESTROYED
                this.x = conflictingBuilding.x!!
                this.y = conflictingBuilding.y!!
            })
        }
    }

    private fun handleGameOver(user: User) {
        if (buildingRepository.countVillagesByUser(user.id!!) > 0) {
            return
        }
        buildingRepository.deleteAllByUser(user.id!!)
        unitRepository.deleteAllByUser(user.id!!)
        userRepository.setBeerTo(user.id!!, START_BEER);
        eventRepository.save(Event().apply {
            this.user1 = user
            this.type = EventType.GAME_OVER
            x = 0
            y = 0
        })

    }

    private fun handleFight(defendingUnit: Unit, attackingUnit: Unit, defendingBuildingType: Building?): Unit {
        val lostCastleAttack = defendingBuildingType != null && checkCastleDefense(defendingBuildingType);
        val looserUnit = when {
            lostCastleAttack -> attackingUnit
            defendingUnit.type == attackingUnit.type -> if (flipCoin()) attackingUnit else defendingUnit
            defendingUnit.type == UnitType.WORKER -> defendingUnit
            attackingUnit.type == UnitType.WORKER -> attackingUnit
            else -> determineLooser(defendingUnit, attackingUnit)
        }
        val winnerUnit = if (looserUnit == defendingUnit) {
            attackingUnit
        } else {
            defendingUnit
        }
        eventRepository.save(Event().apply {
            this.user1 = looserUnit.user
            this.user2 = winnerUnit.user
            this.type = EventType.LOST_UNIT
            this.x = defendingUnit.x!!
            this.y = defendingUnit.y!!
        })
        unitRepository.deleteById(looserUnit.id!!)
        return winnerUnit
    }

    private fun determineLooser(unit1: Unit, unit2: Unit): Unit {
        return when {
            unit1.type == UnitType.DRAGON && unit2.type == UnitType.ARCHER -> unit1 // Archer wins against Dragon
            unit1.type == UnitType.ARCHER && unit2.type == UnitType.DRAGON -> unit2 // Archer wins against Dragon
            unit1.type == UnitType.DRAGON && unit2.type != UnitType.ARCHER -> unit2 // Dragon wins against everything else
            unit1.type != UnitType.ARCHER && unit2.type == UnitType.DRAGON -> unit1 // Dragon wins against everything else
            unit1.type == UnitType.ARCHER && unit2.type != UnitType.DRAGON -> unit1 // Archer loses against everything else
            unit1.type != UnitType.DRAGON && unit2.type == UnitType.ARCHER -> unit2 // Archer loses against everything else
            unit1.type == UnitType.HORSEMAN && unit2.type == UnitType.SPEARMAN -> unit1 // Spearmen wins against Horseman
            unit1.type == UnitType.SPEARMAN && unit2.type == UnitType.SWORDSMAN -> unit1 // Swordsmen wins against Spearmen
            unit1.type == UnitType.SWORDSMAN && unit2.type == UnitType.HORSEMAN -> unit1 // Horseman wins against Swordsmen
            else -> unit2
        }
    }

    fun getUsersUnits(userId: Int): List<UnitDto> {
        return unitRepository.findAllByUserAsDto(userId)
    }
}
