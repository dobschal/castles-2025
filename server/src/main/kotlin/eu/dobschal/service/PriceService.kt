package eu.dobschal.service

import eu.dobschal.model.dto.BuildingDto
import eu.dobschal.model.dto.UnitDto
import eu.dobschal.model.dto.UserDto
import eu.dobschal.model.dto.response.PricesResponseDto
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.UnitRepository
import eu.dobschal.utils.*
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import mu.KotlinLogging
import kotlin.math.pow

@ApplicationScoped
class PriceService @Inject constructor(
    private val unitRepository: UnitRepository,
    private val userService: UserService,
    private val buildingRepository: BuildingRepository
) {

    private val logger = KotlinLogging.logger {}
    private lateinit var unitCreationPrices: Map<UnitType, List<Int>>
    private lateinit var buildingCreationPrices: Map<BuildingType, List<Int>>

    @Startup
    fun calculateAndCachePrices() {
        unitCreationPrices = UnitType.entries.associateWith { type ->
            val prices = mutableListOf<Int>()
            val basePrice = when (type) {
                UnitType.WORKER -> WORKER_BASE_PRICE
                UnitType.SPEARMAN -> SPEARMAN_BASE_PRICE
                UnitType.SWORDSMAN -> SWORDSMAN_BASE_PRICE
                UnitType.HORSEMAN -> HORSEMAN_BASE_PRICE
            }
            for (i in 0..1000) { // If a user has more than 1000 units this will fail...
                val price = basePrice * 2.0.pow(i)
                prices.add(price.toInt())
            }
            prices
        }
        buildingCreationPrices = BuildingType.entries.associateWith { type ->
            val prices = mutableListOf<Int>()
            for (i in 0..1000) {
                val basePrice = when (type) {
                    BuildingType.VILLAGE -> VILLAGE_BASE_PRICE
                    BuildingType.CASTLE -> CASTLE_BASE_PRICE
                    BuildingType.BREWERY -> BREWERY_BASE_PRICE
                    BuildingType.FARM -> FARM_BASE_PRICE
                }
                val price = basePrice * 2.0.pow(i)
                prices.add(price.toInt())
            }
            prices
        }
    }

    fun getPriceForUnitCreation(
        user: UserDto,
        type: UnitType,
        units: List<UnitDto>? = null
    ): Int {
        return unitCreationPrices[type]!![(units ?: unitRepository.findAllByUser(user.id!!)).size]
    }

    fun getPriceForUnitMove(type: UnitType): Int {
        return when (type) {
            UnitType.WORKER -> WORKER_MOVE_PRICE
            UnitType.SPEARMAN -> SPEARMAN_MOVE_PRICE
            UnitType.SWORDSMAN -> SWORDSMAN_MOVE_PRICE
            UnitType.HORSEMAN -> HORSEMAN_MOVE_PRICE
        }
    }

    fun getPriceForBuildingCreation(
        user: UserDto,
        type: BuildingType,
        buildings: List<BuildingDto>? = null
    ): Int {
        val b = buildings ?: buildingRepository.findAllByUser(user.id!!)
        return when (type) {
            BuildingType.VILLAGE -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.VILLAGE }]
            BuildingType.CASTLE -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.CASTLE }]
            BuildingType.BREWERY -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.BREWERY }]
            BuildingType.FARM -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.FARM }]
        }
    }

    fun getAllPrices(): PricesResponseDto {
        val t1 = System.currentTimeMillis()
        val user = userService.getCurrentUserDto()
        val buildings = buildingRepository.findAllByUser(user.id!!)
        val units = unitRepository.findAllByUser(user.id!!)
        logger.info { "Time to get buildings and units: ${System.currentTimeMillis() - t1}ms" }
        val t2 = System.currentTimeMillis()
        val unitCreationPrices = UnitType.entries.associateWith { getPriceForUnitCreation(user, it, units) }
        val unitMovePrices = UnitType.entries.associateWith { getPriceForUnitMove(it) }
        val buildingsPrices = BuildingType.entries.associateWith { getPriceForBuildingCreation(user, it, buildings) }
        logger.info { "Time to calculate prices: ${System.currentTimeMillis() - t2}ms" }
        return PricesResponseDto(
            unitCreationPrices,
            unitMovePrices,
            buildingsPrices
        )

    }
}
