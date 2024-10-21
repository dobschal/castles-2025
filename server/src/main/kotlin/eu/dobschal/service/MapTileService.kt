package eu.dobschal.service

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

    fun getMapTiles(x1Param: Int, x2Param: Int, y1Param: Int, y2Param: Int): List<MapTile> {
        val x1 = min(MAP_MAX, max(-MAP_MAX, x1Param))
        val x2 = min(MAP_MAX, max(-MAP_MAX, x2Param))
        val y1 = min(MAP_MAX, max(-MAP_MAX, y1Param))
        val y2 = min(MAP_MAX, max(-MAP_MAX, y2Param))
        val amountOfMapTiles = (x2 - x1) * (y2 - y1);
        val mapTiles = mapTileRepository.findMapTilesBetween(x1, x2, y1, y2).toMutableList();
        val newMapTiles = emptySet<MapTile>().toMutableSet()
        if (mapTiles.size < amountOfMapTiles) {
            val t1 = System.currentTimeMillis()
            for (x in x1 until x2) {
                for (y in y1 until y2) {
                    if (!mapTiles.any { it.x == x && it.y == y }) {
                        val newMapTile = MapTile().apply {
                            this.x = x
                            this.y = y
                            this.type = MapTileType.pickOneRandomly()
                        }
                        mapTiles.add(newMapTile)
                        newMapTiles.add(newMapTile)
                    }
                }
            }
            val t2 = System.currentTimeMillis()
            logger.info { "Creating new map tiles took ${t2 - t1}ms" }
            if (newMapTiles.isNotEmpty()) {

                // TODO: This is very slow... >7sec for 10_000 tiles ... already indexed

                mapTileRepository.saveMapTiles(newMapTiles)
            }
            val t3 = System.currentTimeMillis()
            logger.info { "Saving new map tiles took ${t3 - t2}ms" }
        }
        return mapTiles
    }

}
