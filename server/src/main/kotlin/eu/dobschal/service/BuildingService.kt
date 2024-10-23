package eu.dobschal.service

import eu.dobschal.model.entity.Building
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.MapTileRepository
import eu.dobschal.repository.UnitRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class BuildingService @Inject constructor(
    private val buildingRepository: BuildingRepository,
    private val mapTileRepository: MapTileRepository,
    private val unitRepository: UnitRepository,
    private val userService: UserService
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
        ensureBuildingPlacement(x, y)
        val startVillage = Building().apply {
            this.x = x
            this.y = y
            this.type = BuildingType.VILLAGE
            this.user = currentUser
        }
        buildingRepository.save(startVillage)
        return startVillage
    }

    fun ensureBuildingPlacement(x: Int, y: Int) {
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
    }

    fun getBuildings(x1: Int, x2: Int, y1: Int, y2: Int) = buildingRepository.findBuildingsBetween(x1, x2, y1, y2)
}
