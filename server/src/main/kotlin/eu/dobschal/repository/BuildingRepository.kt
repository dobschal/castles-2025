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
}
