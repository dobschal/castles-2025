package eu.dobschal.service

import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.MapTileRepository
import eu.dobschal.repository.UnitRepository
import eu.dobschal.repository.UserRepository
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import mu.KotlinLogging
import java.util.*
import kotlin.math.abs

@ApplicationScoped
class BarbarianService @Inject constructor(
    private val userRepository: UserRepository,
    private val unitRepository: UnitRepository,
    private val buildingRepository: BuildingRepository,
    private val mapTileRepository: MapTileRepository,
    private val unitService: UnitService
) {

    val logger = KotlinLogging.logger {}

    @Scheduled(every = "1h")
    fun controlBarbarians() {
        logger.info { "Checking barbarians" }
        val t1 = System.currentTimeMillis()
        val barbarianUser = userRepository.findByUsername("barbarian") ?: userRepository.createUser(
            "barbarian",
            UUID.randomUUID().toString()
        )
        val amountOfWantedBarbarianUnits = userRepository.countUsers() - 1
        val amountOfBarbarianUnits = unitRepository.countUnitsByUser(barbarianUser.id!!)
        val difference = amountOfWantedBarbarianUnits - amountOfBarbarianUnits
        if (difference > 0) {
            createRandomBarbarianUnits(difference, barbarianUser)
        }
        moveBarbarianUnits(barbarianUser)
        logger.info { "Barbarians checked in ${System.currentTimeMillis() - t1}ms" }
    }

    fun moveBarbarianUnits(barbarianUser: User) {
        val mapTiles = mapTileRepository.listAll()
        val units = unitRepository.findAllByUser(barbarianUser.id!!)
        for (unit in units) {
            findMapTileToMoveTo(unit, unit.x!!, unit.y!!, mapTiles, units, barbarianUser).let { (x, y) ->
                val (conflictingBuilding, conflictingUnit) = unitService.getMoveConflictingBuildingsAndUnits(
                    x,
                    y,
                    unit,
                    barbarianUser
                )
                unitRepository.updatePosition(unit.id!!, x, y)
                unitService.handleFightAndConquer(conflictingUnit, barbarianUser, unit, conflictingBuilding)
                logger.info { "Moved barbarian unit from ${unit.x}, ${unit.y} to $x, $y" }
            }
        }
    }

    fun findMapTileToMoveTo(
        unit: Unit,
        x: Int,
        y: Int,
        mapTiles: List<MapTile>,
        units: List<Unit>,
        barbarianUser: User
    ): Pair<Int, Int> {
        val possibleTiles =
            mapTiles.filter {
                val xDiff = abs(it.x!! - x)
                val yDiff = abs(it.y!! - y)
                if (xDiff > 1 || yDiff > 1) {
                    return@filter false
                }
                val hasConflictingUnit =
                    units.any { it.id != unit.id && it.x == x && it.y == y && barbarianUser.id == it.user?.id }
                return@filter it.type != MapTileType.WATER
                        && it.x != x
                        && it.y != y
                        && !hasConflictingUnit
            }
        return possibleTiles.random().let { Pair(it.x!!, it.y!!) }
    }

    fun createRandomBarbarianUnits(amount: Int, barbarianUser: User) {
        for (i in 0 until amount) {
            findEmptyMapTileForBarbarian()?.let { (x, y) ->
                unitRepository.save(Unit().apply {
                    this.x = x
                    this.y = y
                    user = barbarianUser
                    type = UnitType.entries.toTypedArray().filter { it != UnitType.WORKER }.random()
                })
                logger.info { "Created barbarian unit at $x, $y" }
            }

        }
    }

    fun findEmptyMapTileForBarbarian(): Pair<Int, Int>? {
        val margin = 10
        val buildings = buildingRepository.listAll()
        val units = unitRepository.listAll()
        val mapTiles = mapTileRepository.listAll()
        val lowestX = buildings.minByOrNull { it.x!! }?.x ?: -margin
        val lowestY = buildings.minByOrNull { it.y!! }?.y ?: -margin
        val highestX = buildings.maxByOrNull { it.x!! }?.x ?: margin
        val highestY = buildings.maxByOrNull { it.y!! }?.y ?: margin
        for (i in 0 until 1000) {
            val x = Random().nextInt(lowestX - margin, highestX + margin)
            val y = Random().nextInt(lowestY - margin, highestY + margin)
            val conflictingBuilding = buildings.any { abs(it.x!! - x) < 5 && abs(it.y!! - y) < 5 }
            val conflictingUnit = units.any { it.x == x && it.y == y }
            val mapTile = mapTiles.find { it.x == x && it.y == y }
            if (!conflictingBuilding && !conflictingUnit && mapTile?.type != MapTileType.WATER) {
                return Pair(x, y)
            }
        }
        logger.error { "Could not find empty map tile for barbarian" }
        return null
    }

}
