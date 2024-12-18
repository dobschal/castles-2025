package eu.dobschal.service

import eu.dobschal.model.dto.BuildingDto
import eu.dobschal.model.dto.UnitDto
import eu.dobschal.model.dto.UserDto
import eu.dobschal.model.dto.response.PricesResponseDto
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.Currency
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
    private lateinit var buildingLevelUpPrices: Map<BuildingType, List<Int>>

    @Startup
    fun calculateAndCachePrices() {
        unitCreationPrices = UnitType.entries.associateWith { type ->
            val prices = mutableListOf<Int>()
            val basePrice = when (type) {
                UnitType.WORKER -> WORKER_BASE_PRICE
                UnitType.SPEARMAN -> SPEARMAN_BASE_PRICE
                UnitType.SWORDSMAN -> SWORDSMAN_BASE_PRICE
                UnitType.HORSEMAN -> HORSEMAN_BASE_PRICE
                UnitType.DRAGON -> DRAGON_BASE_PRICE
                UnitType.ARCHER -> ARCHER_BASE_PRICE
            }
            for (i in 0..1000) { // If a user has more than 1000 units this will fail...
                val price = basePrice * (UNIT_PRICE_FACTOR).pow(i)
                prices.add(price.toInt())
            }
            prices
        }
        buildingCreationPrices = BuildingType.entries.associateWith { type ->
            val prices = mutableListOf<Int>()
            for (i in 0..1000) {
                val basePrice = when (type) {
                    BuildingType.CITY -> CITY_BASE_PRICE
                    BuildingType.VILLAGE -> VILLAGE_BASE_PRICE
                    BuildingType.CASTLE -> CASTLE_BASE_PRICE
                    BuildingType.BREWERY -> BREWERY_BASE_PRICE
                    BuildingType.FARM -> FARM_BASE_PRICE
                    BuildingType.MARKET -> MARKET_BASE_PRICE
                }
                val factor = when (type) {
                    BuildingType.CITY -> 3.0
                    BuildingType.MARKET -> 3.0
                    else -> 2.0
                }
                val price = basePrice * factor.pow(i)
                prices.add(price.toInt())
            }
            prices
        }
        buildingLevelUpPrices = BuildingType.entries.associateWith { type ->
            val prices = mutableListOf<Int>()
            for (i in 0..1000) {
                val basePrice = when (type) {
                    BuildingType.CITY -> CITY_LEVEL_UP_PRICE
                    BuildingType.VILLAGE -> VILLAGE_LEVEL_UP_PRICE
                    BuildingType.CASTLE -> CASTLE_LEVEL_UP_PRICE
                    BuildingType.BREWERY -> BREWERY_LEVEL_UP_PRICE
                    BuildingType.FARM -> FARM_LEVEL_UP_PRICE
                    BuildingType.MARKET -> MARKET_LEVEL_UP_PRICE
                }
                val factor = 3.0
                val price = basePrice * factor.pow(i)
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
        return unitCreationPrices[type]!![(units ?: unitRepository.findAllByUserAsDto(user.id!!)).size]
    }

    fun getPriceForUnitMove(type: UnitType): Int {
        return when (type) {
            UnitType.WORKER -> WORKER_MOVE_PRICE
            UnitType.SPEARMAN -> SPEARMAN_MOVE_PRICE
            UnitType.SWORDSMAN -> SWORDSMAN_MOVE_PRICE
            UnitType.HORSEMAN -> HORSEMAN_MOVE_PRICE
            UnitType.DRAGON -> DRAGON_MOVE_PRICE
            UnitType.ARCHER -> ARCHER_MOVE_PRICE
        }
    }

    fun getPriceForBuildingCreation(
        user: UserDto,
        type: BuildingType,
        buildings: List<BuildingDto>? = null
    ): Int {
        val b = buildings ?: buildingRepository.findAllByUser(user.id!!)
        return when (type) {

            // When creating a city, the village is deleted. But still the village price should raise.
            BuildingType.VILLAGE -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.VILLAGE || it.type == BuildingType.CITY }]

            BuildingType.CITY -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.CITY }]
            BuildingType.CASTLE -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.CASTLE }]
            BuildingType.BREWERY -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.BREWERY }]
            BuildingType.FARM -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.FARM }]
            BuildingType.MARKET -> buildingCreationPrices[type]!![b.count { it.type == BuildingType.MARKET }]
        }
    }

    fun getPriceForBuildingLevelUp(
        user: UserDto,
        type: BuildingType,
        level: Int,
        buildings: List<BuildingDto>? = null
    ): Int {
        val b = buildings ?: buildingRepository.findAllByUser(user.id!!)
        return when (type) {
            BuildingType.VILLAGE -> buildingLevelUpPrices[type]!![b.count { it.type == BuildingType.VILLAGE && it.level == level }]
            BuildingType.CITY -> buildingLevelUpPrices[type]!![b.count { it.type == BuildingType.CITY && it.level == level }]
            BuildingType.CASTLE -> buildingLevelUpPrices[type]!![b.count { it.type == BuildingType.CASTLE && it.level == level }]
            BuildingType.BREWERY -> buildingLevelUpPrices[type]!![b.count { it.type == BuildingType.BREWERY && it.level == level }]
            BuildingType.FARM -> buildingLevelUpPrices[type]!![b.count { it.type == BuildingType.FARM && it.level == level }]
            BuildingType.MARKET -> buildingLevelUpPrices[type]!![b.count { it.type == BuildingType.MARKET && it.level == level }]
        }
    }

    fun getAllPrices(): PricesResponseDto {
        val user = userService.getCurrentUserDto()
        val buildings = buildingRepository.findAllByUser(user.id!!)
        val units = unitRepository.findAllByUserAsDto(user.id!!)
        val unitCreationPrices = UnitType.entries.associateWith { getPriceForUnitCreation(user, it, units) }
        val unitMovePrices = UnitType.entries.associateWith { getPriceForUnitMove(it) }
        val buildingsPrices = BuildingType.entries.associateWith { getPriceForBuildingCreation(user, it, buildings) }
        val buildingLevelUpPrices =
            BuildingType.entries.associateWith { getPriceForBuildingLevelUp(user, it, 2, buildings) }
        return PricesResponseDto(
            unitCreationPrices,
            unitMovePrices,
            buildingsPrices,
            buildingLevelUpPrices,
            SELL_BEER_PRICE
        )

    }

    fun getUnitsCurrency(type: UnitType): Currency {
        return when (type) {
            UnitType.WORKER -> Currency.BEER
            UnitType.SPEARMAN -> Currency.BEER
            UnitType.SWORDSMAN -> Currency.BEER
            UnitType.HORSEMAN -> Currency.BEER
            UnitType.DRAGON -> Currency.GOLD
            UnitType.ARCHER -> Currency.GOLD
        }
    }
}
