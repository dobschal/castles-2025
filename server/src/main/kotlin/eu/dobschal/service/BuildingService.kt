package eu.dobschal.service

import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.Event
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException

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
        val currentUserId = userService.getCurrentUser().id!!
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

    fun getBuildings(x1: Int, x2: Int, y1: Int, y2: Int) = buildingRepository.findBuildingsBetween(x1, x2, y1, y2)

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
        val price = priceService.getPriceForBuildingCreation(currentUser, type)
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

        // TODO: Ensure Farm next to Brewery

        unitRepository.deleteById(unit.id!!)
        userRepository.deductBeerFromUser(currentUser.id!!, price)
        return persistBuilding(x, y, type, currentUser)
    }

    private fun persistBuilding(
        x: Int,
        y: Int,
        type: BuildingType,
        currentUser: User
    ): Building {
        val building = Building().apply {
            this.x = x
            this.y = y
            this.type = type
            this.user = currentUser
        }
        buildingRepository.save(building)
        eventRepository.save(Event().apply {
            this.user1 = currentUser
            this.type = EventType.BUILDING_CREATED
            this.building = building
            this.x = x
            this.y = y
        })
        return building
    }


}
