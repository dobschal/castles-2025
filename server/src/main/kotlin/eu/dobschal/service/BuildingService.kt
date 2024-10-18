package eu.dobschal.service

import eu.dobschal.model.entity.Building
import eu.dobschal.repository.BuildingRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException

@ApplicationScoped
class BuildingService @Inject constructor(
    private val buildingRepository: BuildingRepository,
    private val userService: UserService
) {
    fun getStartVillage(): Building {
        val currentUserId = userService.getCurrentUser().id!!
        val startVillage = buildingRepository.findUsersStartVillage(currentUserId)
        return startVillage ?: throw NotFoundException("Start village not found")
    }
}
