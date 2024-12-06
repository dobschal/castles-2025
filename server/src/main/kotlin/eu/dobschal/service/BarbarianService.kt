package eu.dobschal.service

import eu.dobschal.model.entity.Event
import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

@ApplicationScoped
class BarbarianService @Inject constructor(
    private val userRepository: UserRepository,
    private val unitRepository: UnitRepository,
    private val buildingRepository: BuildingRepository,
    private val mapTileRepository: MapTileRepository,
    private val unitService: UnitService,
    private val eventRepository: EventRepository
) {

    val logger = KotlinLogging.logger {}

    @Scheduled(every = "65m")
    fun controlBarbarians() {
        logger.info { "Checking barbarians" }
        val t1 = System.currentTimeMillis()
        val barbarianUser = userRepository.findByUsername("barbarian") ?: userRepository.createUser(
            "barbarian",
            UUID.randomUUID().toString()
        )
        deleteOldBarbarianUnits(barbarianUser)
        val amountOfWantedBarbarianUnits = ceil(userRepository.countUsers().toDouble() * 2).toInt()
        val amountOfBarbarianUnits = unitRepository.countUnitsByUser(barbarianUser.id!!, listOf(UnitType.WORKER))
        val difference = amountOfWantedBarbarianUnits - amountOfBarbarianUnits
        if (difference > 0) {
            createRandomBarbarianUnits(difference, barbarianUser)
        }
        moveBarbarianUnits(barbarianUser)
        logger.info { "Barbarians checked in ${System.currentTimeMillis() - t1}ms" }
    }

    private fun deleteOldBarbarianUnits(barbarianUser: User) {
        val units = unitRepository.findAllByUser(barbarianUser.id!!)
        for (unit in units) {
            if (LocalDateTime.now().minusHours(24).isAfter(unit.createdAt)) {
                unitRepository.deleteById(unit.id!!)
                logger.info { "Deleted too old barbarian unit" }
            }
        }
    }

    fun moveBarbarianUnits(barbarianUser: User) {
        val mapTiles = mapTileRepository.listAll()
        val units = unitRepository.findAllByUser(barbarianUser.id!!)
        for (unit in units) {
            // In case is n't a map tile yet, we stop, so the unit would stuck there, but that is fine
            findMapTileToMoveTo(unit, unit.x!!, unit.y!!, mapTiles, units, barbarianUser)?.let { (x, y) ->
                try {
                    val (conflictingBuilding, conflictingUnit) = unitService.getMoveConflictingBuildingsAndUnits(
                        x,
                        y,
                        unit,
                        barbarianUser
                    )
                    eventRepository.save(Event().apply {
                        this.user1 = barbarianUser
                        this.type = EventType.UNIT_MOVED
                        this.unit = unit
                        this.x = x
                        this.y = y
                    })
                    unitRepository.updatePosition(unit.id!!, x, y)
                    unitService.handleFightAndConquer(conflictingUnit, barbarianUser, unit, conflictingBuilding)
                    logger.info { "Moved barbarian unit from ${unit.x}, ${unit.y} to $x, $y" }
                } catch (e: Exception) {
                    logger.error(e) { "Could not move barbarian unit from ${unit.x}, ${unit.y} to $x, $y" }
                }
            } ?: logger.info { "Could not find map tile to move barbarian unit from ${unit.x}, ${unit.y}" }
        }
    }

    fun findMapTileToMoveTo(
        unit: Unit,
        x: Int,
        y: Int,
        mapTiles: List<MapTile>,
        units: List<Unit>,
        barbarianUser: User
    ): Pair<Int, Int>? {
        val direction = unit.id!! % 4 // 0 means north, 1 east, 2 south and 3 west
        val possibleTiles =
            mapTiles.filter {
                val xDiff = it.x!! - x
                val yDiff = it.y!! - y
                if (abs(xDiff) > 1 || abs(yDiff) > 1) {
                    return@filter false
                }
                if (direction == 0 && yDiff != -1) {
                    return@filter false
                }
                if (direction == 1 && xDiff != 1) {
                    return@filter false
                }
                if (direction == 2 && yDiff != 1) {
                    return@filter false
                }
                if (direction == 3 && xDiff != -1) {
                    return@filter false
                }
                val hasConflictingUnit =
                    units.any { it.id != unit.id && it.x == x && it.y == y && barbarianUser.id == it.user?.id }
                return@filter it.type != MapTileType.WATER
                        && it.x != x
                        && it.y != y
                        && !hasConflictingUnit
            }
        if (possibleTiles.isEmpty()) {
            return null
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
                    type = UnitType.entries.toTypedArray()
                        .filter { it != UnitType.WORKER && it != UnitType.DRAGON && it != UnitType.ARCHER }
                        .random()
                })
                logger.info { "Created barbarian unit at $x, $y" }
            }

        }
    }

    fun findEmptyMapTileForBarbarian(): Pair<Int, Int>? {
        val margin = 1
        val minSpawnDistance = 3
        val buildings = buildingRepository.listAll()
        val units = unitRepository.listAll()
        val mapTiles = mapTileRepository.listAll()
        val lowestX = buildings.minByOrNull { it.x!! }?.x ?: -margin
        val lowestY = buildings.minByOrNull { it.y!! }?.y ?: -margin
        val highestX = buildings.maxByOrNull { it.x!! }?.x ?: margin
        val highestY = buildings.maxByOrNull { it.y!! }?.y ?: margin
        for (i in 0 until 2000) {
            val x = kotlin.random.Random.nextInt(lowestX - margin, highestX + margin)
            val y = kotlin.random.Random.nextInt(lowestY - margin, highestY + margin)
            val conflictingBuilding =
                buildings.any { abs(it.x!! - x) < minSpawnDistance && abs(it.y!! - y) < minSpawnDistance }
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
