package eu.dobschal.repository

import eu.dobschal.model.dto.BuildingDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.enum.BuildingType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@Transactional
@ApplicationScoped
class BuildingRepository : PanacheRepository<Building> {

    fun findById(id: Int): Building? {
        return find("id = ?1", id).firstResult()
    }

    fun findUsersStartVillage(userId: Int): Building? {
        return find("user.id = ?1 AND type = ?2 ORDER BY createdAt ASC", userId, BuildingType.VILLAGE).firstResult()
    }

    fun save(building: Building) {
        persist(building)
    }

    fun findBuildingsBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<BuildingDto> {
        return find("#Building.findBuildingsBetween", x1, x2, y1, y2)
            .project(BuildingDto::class.java)
            .list()
    }

    fun findBuildingByXAndY(x: Int, y: Int): Building? {
        return find("x = ?1 and y = ?2", x, y).firstResult()
    }

    fun findAllByUser(userId: Int): List<BuildingDto> {
        return find("#Building.findAllByUser", userId)
            .project(BuildingDto::class.java)
            .list()
    }

    fun countVillagesByUser(userId: Int): Int {
        return count("user.id = ?1 AND type = ?2", userId, BuildingType.VILLAGE).toInt()
    }

    fun countCastlesByUser(userId: Int): Int {
        return count("user.id = ?1 AND type = ?2", userId, BuildingType.CASTLE).toInt()
    }

    fun updateOwner(buildingId: Int, userId: Int) {
        update("user.id = ?1 where id = ?2", userId, buildingId)
    }
}
