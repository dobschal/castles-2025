package eu.dobschal.repository

import eu.dobschal.model.dto.MapTileDto
import eu.dobschal.model.entity.MapTile
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional


@Transactional
@ApplicationScoped
class MapTileRepository @Inject constructor(
    private val entityManager: EntityManager
) : PanacheRepository<MapTile> {

    fun findByXAndY(x: Int, y: Int): MapTile? {
        return find("x = ?1 and y = ?2", x, y).firstResult()
    }

    fun findMapTilesBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<MapTileDto> {
        return find("#MapTile.findMapTilesBetween", x1, x2, y1, y2)
            .project(MapTileDto::class.java)
            .list()
    }

    // We are using a native query to insert the values because it is faster than using Panache
    fun saveMapTiles(mapTiles: Set<MapTile>) {
        val query = mapTiles.map {
            "INSERT INTO map_tile (x, y, type) VALUES (${it.x}, ${it.y}, '${it.type}')"
        }.joinToString(separator = ";")
        entityManager.createNativeQuery(query).executeUpdate()
    }

}
