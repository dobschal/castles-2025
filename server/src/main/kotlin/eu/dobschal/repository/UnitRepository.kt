package eu.dobschal.repository

import eu.dobschal.model.entity.Unit
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@Transactional
@ApplicationScoped
class UnitRepository : PanacheRepository<Unit> {

    fun save(unit: Unit) {
        persist(unit)
    }

    fun findUnitsBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<Unit> {
        return find("x >= ?1 and x < ?2 and y >= ?3 and y < ?4", x1, x2, y1, y2).list()
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

    fun findAllByUser(id: Int): List<Unit> {
        return find("user.id", id).list()

    }
}
