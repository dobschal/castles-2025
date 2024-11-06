package eu.dobschal.service

import eu.dobschal.model.dto.BuildingDto
import eu.dobschal.model.dto.response.BuildingsResponseDto
import eu.dobschal.model.dto.response.SuccessResponseDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.Event
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
import eu.dobschal.utils.BREWERY_BEER_PRODUCTION_PER_HOUR
import eu.dobschal.utils.BREWERY_BEER_STORAGE
import eu.dobschal.utils.GOLD_STORAGE_PER_CITY
import eu.dobschal.utils.VILLAGE_BASE_PRICE
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.pow

@ApplicationScoped
class BuildingService @Inject constructor(
    private val buildingRepository: BuildingRepository,
    private val mapTileRepository: MapTileRepository,
    private val unitRepository: UnitRepository,
    private val userService: UserService,
    private val eventRepository: EventRepository,
    private val priceService: PriceService,
    private val userRepository: UserRepository
) {
    fun getStartVillage(): Building {
        val currentUserId = userService.getCurrentUserDto().id!!
        val startVillage = buildingRepository.findUsersStartVillage(currentUserId)
        return startVillage ?: throw NotFoundException("serverError.noStartVillage")
    }

    fun saveStartVillage(x: Int, y: Int): Building {
        val currentUser = userService.getCurrentUser()
        buildingRepository.findUsersStartVillage(currentUser.id!!)?.let {
            throw BadRequestException("serverError.startVillageAlreadyExists")
        }
        val mapTile =
            mapTileRepository.findByXAndY(x, y) ?: throw BadRequestException("serverError.wrongTileType")
        if (mapTile.type != MapTileType.PLAIN) {
            throw BadRequestException("serverError.wrongTileType")
        }
        val buildingsAround = buildingRepository.findBuildingsBetween(x - 2, x + 3, y - 2, y + 3)
        if (buildingsAround.isNotEmpty()) {
            throw BadRequestException("serverError.conflictingBuilding")
        }
        val conflictingUnit = unitRepository.findUnitByXAndY(x, y)
        if (conflictingUnit != null) {
            throw BadRequestException("serverError.conflictingUnit")
        }
        return persistBuilding(x, y, BuildingType.VILLAGE, currentUser)
    }

    fun getBuildings(x1: Int, x2: Int, y1: Int, y2: Int): BuildingsResponseDto {
        val buildings = buildingRepository.findBuildingsBetween(x1, x2, y1, y2)
        val currentUser = userService.getCurrentUser()
        return BuildingsResponseDto(
            buildings,
            BREWERY_BEER_PRODUCTION_PER_HOUR,
            BREWERY_BEER_STORAGE,
            getTotalBeerStorage(currentUser.id!!),
            buildingRepository.countVillagesByUser(currentUser.id!!),
            getTotalGoldStorage(currentUser.id!!),
        )
    }

    fun getTotalGoldStorage(userId: Int): Int {
        val usersBuildings = buildingRepository.findAllByUser(userId)
        val storageFactor = GOLD_STORAGE_PER_CITY;
        val amountOfVillagesAndCities =
            usersBuildings.count { it.type == BuildingType.CITY }
        return storageFactor * amountOfVillagesAndCities
    }

    fun getTotalBeerStorage(userId: Int): Int {
        val usersBuildings = buildingRepository.findAllByUser(userId)
        val storageFactor = VILLAGE_BASE_PRICE;
        val amountOfVillagesAndCities =
            usersBuildings.count { it.type == BuildingType.VILLAGE || it.type == BuildingType.CITY }
        return (storageFactor * 2.0.pow(amountOfVillagesAndCities)).toInt()
    }

    fun createBuilding(x: Int, y: Int, type: BuildingType): Building {
        val currentUser = userService.getCurrentUser()
        val unit = unitRepository.findUnitByXAndY(x, y) ?: throw BadRequestException("serverError.noUnit")
        if ((unit.type != UnitType.WORKER) || (unit.user?.id != currentUser.id)) {
            throw BadRequestException("serverError.workerRequired")
        }
        val mapTile =
            mapTileRepository.findByXAndY(x, y) ?: throw BadRequestException("serverError.wrongTileType")
        if (mapTile.type != MapTileType.PLAIN) {
            throw BadRequestException("serverError.wrongTileType")
        }
        val price = priceService.getPriceForBuildingCreation(currentUser.toDto(), type)
        if (currentUser.beer!! < price) {
            throw BadRequestException("serverError.notEnoughBeer")
        }
        if (buildingRepository.findBuildingByXAndY(x, y) != null) {
            throw BadRequestException("serverError.conflictingBuilding")
        }
        if (type == BuildingType.BREWERY) {
            val buildingsAround = buildingRepository.findBuildingsBetween(x - 1, x + 2, y - 1, y + 2)
            buildingsAround.find { it.type == BuildingType.FARM && it.user?.id == currentUser.id }
                ?: throw BadRequestException("serverError.noFarm")
        }
        // Per village, the user can only have one brewery, farm or castle
        val amountOfVillages = buildingRepository.countVillagesByUser(currentUser.id!!)
        val amountOfCities = buildingRepository.countCitiesByUser(currentUser.id!!)
        val buildings = buildingRepository.findAllByUser(currentUser.id!!)
        if (type == BuildingType.FARM || type == BuildingType.CASTLE) {
            if (buildings.count { it.type == type } >= amountOfVillages + amountOfCities) {
                throw BadRequestException("serverError.onlyOnePerVillage")
            }
        } else if (type == BuildingType.BREWERY) {
            if (buildings.count { it.type == type } >= (amountOfVillages + amountOfCities) * 2) {
                throw BadRequestException("serverError.onlyOnePerVillage")
            }
        }
        unitRepository.deleteById(unit.id!!)
        userRepository.deductBeerFromUser(currentUser.id!!, price)
        return persistBuilding(x, y, type, currentUser)
    }

    fun destroyBuilding(x: Int, y: Int): SuccessResponseDto {
        val currentUser = userService.getCurrentUser()

        val building = buildingRepository.findBuildingByXAndY(x, y) ?: throw NotFoundException("serverError.noBuilding")
        if (building.user?.id != currentUser.id) {
            throw BadRequestException("serverError.notYourBuilding")
        }

        if (building.type == BuildingType.VILLAGE || building.type == BuildingType.CITY) {
            // if it's a village check if it's his last village
            val amountOfVillages = buildingRepository.countVillagesByUser(currentUser.id!!)
            val amountOfCities = buildingRepository.countCitiesByUser(currentUser.id!!)
            if (amountOfVillages + amountOfCities < 2) {
                throw BadRequestException("serverError.lastVillage")
            }
        }

        buildingRepository.delete(building)
        eventRepository.save(Event().apply {
            this.user1 = currentUser
            this.type = EventType.BUILDING_DESTROYED
            this.x = x
            this.y = y
        })
        return SuccessResponseDto("serverSuccess.buildingDestroyed")
    }

    private fun persistBuilding(
        x: Int,
        y: Int,
        type: BuildingType,
        user: User
    ): Building {
        val building = Building().apply {
            this.x = x
            this.y = y
            this.type = type
            this.user = user
        }
        buildingRepository.save(building)
        eventRepository.save(Event().apply {
            this.user1 = user
            this.type = EventType.BUILDING_CREATED
            this.building = building
            this.x = x
            this.y = y
        })
        return building
    }

    fun collectBeer(buildingId: Int, amountOfBeer: Int): SuccessResponseDto {
        val building = buildingRepository.findById(buildingId) ?: throw NotFoundException("serverError.noBuilding")
        if (building.type != BuildingType.BREWERY) {
            throw BadRequestException("serverError.notABrewery")
        }
        if (amountOfBeer <= 0) {
            throw BadRequestException("serverError.invalidAmountOfBeer")
        }
        val currentUser = userService.getCurrentUser()
        if (building.user?.id != currentUser.id) {
            throw BadRequestException("serverError.notYourBuilding")
        }
        val buildingsAround =
            buildingRepository.findBuildingsBetween(
                building.x!! - 1,
                building.x!! + 2,
                building.y!! - 1,
                building.y!! + 2
            )
        buildingsAround.find { it.type == BuildingType.FARM && it.user?.id == currentUser.id }
            ?: throw BadRequestException("serverError.noFarm")
        val event = eventRepository.findEventByXAndYAndType(building.x!!, building.y!!, EventType.BEER_COLLECTED)
        val lastCollectedAt = event?.createdAt ?: LocalDateTime.of(1970, 1, 1, 0, 0)
        val timeSinceLastCollection = LocalDateTime.now().atZone(ZoneId.systemDefault())
            .toEpochSecond() - lastCollectedAt.atZone(ZoneId.systemDefault()).toEpochSecond()
        var beerProducedSinceLastCollection =
            min(
                BREWERY_BEER_STORAGE,
                floor((timeSinceLastCollection.toDouble() / 3600) * BREWERY_BEER_PRODUCTION_PER_HOUR.toDouble()).toInt()
            )
        val beerLimit = getTotalBeerStorage(currentUser.id!!);
        if (beerProducedSinceLastCollection + currentUser.beer!! > beerLimit) {
            beerProducedSinceLastCollection = beerLimit - currentUser.beer!!
        }
        userRepository.addBeerToUser(currentUser.id!!, beerProducedSinceLastCollection)
        eventRepository.save(Event().apply {
            this.user1 = currentUser
            this.type = EventType.BEER_COLLECTED
            this.building = building
            this.x = building.x
            this.y = building.y
        })
        return SuccessResponseDto("serverSuccess.beerCollected")
    }

    fun createCity(x: Int, y: Int, type: BuildingType): SuccessResponseDto {
        val currentUser = userService.getCurrentUser()
        if (type != BuildingType.CITY) {
            throw BadRequestException("serverError.notACity")
        }
        val village = buildingRepository.findBuildingByXAndY(x, y) ?: throw NotFoundException("serverError.noVillage")
        if (village.type != BuildingType.VILLAGE) {
            throw BadRequestException("serverError.notAVillage")
        }
        if (village.user?.id != currentUser.id) {
            throw BadRequestException("serverError.notYourBuilding")
        }
        val price = priceService.getPriceForBuildingCreation(currentUser.toDto(), type)
        if (currentUser.beer!! < price) {
            throw BadRequestException("serverError.notEnoughBeer")
        }
        buildingRepository.delete(village)
        userRepository.deductBeerFromUser(currentUser.id!!, price)
        persistBuilding(x, y, type, currentUser)
        eventRepository.save(Event().apply {
            this.user1 = currentUser
            this.type = EventType.BUILDING_CREATED
            this.x = x
            this.y = y
        })
        return SuccessResponseDto("serverSuccess.cityCreated")
    }

    fun getUsersBuildings(userId: Int): List<BuildingDto> {
        return buildingRepository.findAllByUser(userId)
    }


}
