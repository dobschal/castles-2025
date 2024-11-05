package eu.dobschal.repository

import eu.dobschal.model.dto.UnitDto
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.UnitType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@Transactional
@ApplicationScoped
class UnitRepository : PanacheRepository<Unit> {

    fun save(unit: Unit) {
        persist(unit)
    }

    fun findUnitsBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<UnitDto> {
        return find("#Unit.findUnitsBetween", x1, x2, y1, y2)
            .project(UnitDto::class.java)
            .list()
    }

    fun findUnitByXAndY(x: Int, y: Int): Unit? {
        return find("x = ?1 and y = ?2", x, y).firstResult()
    }

    fun findById(id: Int): Unit? {
        return find("id", id).firstResult()
    }

    fun updatePosition(id: Int, x: Int, y: Int) {
        update("x = ?1, y = ?2 where id = ?3", x, y, id)
    }

    fun findAllByUserAsDto(userId: Int): List<UnitDto> {
        return find("#Unit.findAllByUser", userId)
            .project(UnitDto::class.java)
            .list()
    }

    fun findAllByUser(userId: Int): List<Unit> {
        return find("user.id = ?1", userId).list()
    }

    fun deleteById(id: Int) {
        delete("id", id)
    }

    fun countUnitsByUser(userId: Int): Int {
        return count("user.id = ?1", userId).toInt()
    }

    fun findUnitByTypeAndUser(userId: Int, unitType: UnitType): Unit? {
        return find("user.id = ?1 and type = ?2", userId, unitType).firstResult()
    }

    fun findUnitByTypeInAndUser(userId: Int, unitTypes: List<UnitType>): Unit? {
        return find("user.id = ?1 and type in ?2", userId, unitTypes).firstResult()
    }

    fun deleteAllByUser(userId: Int) {
        delete("user.id = ?1", userId)
    }
}
