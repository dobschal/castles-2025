package eu.dobschal.service

import eu.dobschal.model.entity.Building
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.MapTileRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class BuildingService @Inject constructor(
    private val buildingRepository: BuildingRepository,
    private val mapTileRepository: MapTileRepository,
    private val userService: UserService
) {
    fun getStartVillage(): Building {
        val currentUserId = userService.getCurrentUser().id!!
        val startVillage = buildingRepository.findUsersStartVillage(currentUserId)
        return startVillage ?: throw NotFoundException("Start village not found")
    }

    fun saveStartVillage(x: Int, y: Int): Building {
        val currentUser = userService.getCurrentUser()
        buildingRepository.findUsersStartVillage(currentUser.id!!)?.let {
            throw BadRequestException("Start village already exists")
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
            mapTileRepository.findByXAndY(x, y) ?: throw BadRequestException("Building placement is not on a map tile")
        if (mapTile.type != MapTileType.PLAIN) {
            throw BadRequestException("Building placement is not on a plain map tile")
        }
        val buildingsAround = buildingRepository.findBuildingsBetween(x - 2, x + 2, y - 2, y + 2)
        if (buildingsAround.isNotEmpty()) {
            throw BadRequestException("Building placement is too close to other buildings")
        }
    }
}
