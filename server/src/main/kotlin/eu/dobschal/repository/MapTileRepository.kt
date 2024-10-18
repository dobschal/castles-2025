package eu.dobschal.repository

import eu.dobschal.model.entity.MapTile
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@Transactional
@ApplicationScoped
class MapTileRepository : PanacheRepository<MapTile> {

    fun findByXAndY(x: Int, y: Int): MapTile? {
        return find("x = ?1 and y = ?2", x, y).firstResult()
    }

    fun findMapTilesBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<MapTile> {
        return find("x >= ?1 and x < ?2 and y >= ?3 and y < ?4", x1, x2, y1, y2).list()
    }

    fun saveMapTiles(mapTiles: List<MapTile>) {
        persist(mapTiles)
    }

}
