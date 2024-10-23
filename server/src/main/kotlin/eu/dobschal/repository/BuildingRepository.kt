package eu.dobschal.repository

import eu.dobschal.model.entity.Building
import eu.dobschal.model.enum.BuildingType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@Transactional
@ApplicationScoped
class BuildingRepository : PanacheRepository<Building> {
    fun findUsersStartVillage(userId: Int): Building? {
        return find("user.id = ?1 AND type = ?2 ORDER BY createdAt ASC", userId, BuildingType.VILLAGE).firstResult()
    }

    fun save(building: Building) {
        persist(building)
    }

    fun findBuildingsBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<Building> {
        return find("x >= ?1 and x < ?2 and y >= ?3 and y < ?4", x1, x2, y1, y2).list()
    }

    fun findBuildingByXAndY(x: Int, y: Int): Building? {
        return find("x = ?1 and y = ?2", x, y).firstResult()
    }
}
