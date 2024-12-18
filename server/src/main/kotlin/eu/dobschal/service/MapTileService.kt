package eu.dobschal.service

import eu.dobschal.model.dto.MapTileDto
import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.repository.MapTileRepository
import eu.dobschal.utils.MAP_MAX
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import mu.KotlinLogging
import kotlin.math.max
import kotlin.math.min

@ApplicationScoped
class MapTileService @Inject constructor(private val mapTileRepository: MapTileRepository) {

    private val logger = KotlinLogging.logger {}

    fun getMapTiles(x1Param: Int, x2Param: Int, y1Param: Int, y2Param: Int): List<MapTileDto> {
        val x1 = min(MAP_MAX, max(-MAP_MAX, x1Param))
        val x2 = min(MAP_MAX, max(-MAP_MAX, x2Param))
        val y1 = min(MAP_MAX, max(-MAP_MAX, y1Param))
        val y2 = min(MAP_MAX, max(-MAP_MAX, y2Param))
        val amountOfMapTiles = (x2 - x1) * (y2 - y1);
        val mapTiles = mapTileRepository.findMapTilesBetween(x1, x2, y1, y2)
        if (mapTiles.size == amountOfMapTiles) {
            return mapTiles
        }
        val newMapTiles = emptySet<MapTile>().toMutableSet()
        for (x in x1 until x2) {
            for (y in y1 until y2) {
                if (!mapTiles.any { it.x == x && it.y == y }) {
                    val newMapTile = MapTile().apply {
                        this.x = x
                        this.y = y
                        this.type = MapTileType.pickOneRandomly()
                    }
                    newMapTiles.add(newMapTile)
                }
            }
        }
        if (newMapTiles.isNotEmpty()) {
            mapTileRepository.saveMapTiles(newMapTiles)
        }
        return getMapTiles(x1, x2, y1, y2)
    }

}
