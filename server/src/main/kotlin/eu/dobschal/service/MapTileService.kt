package eu.dobschal.service

import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.repository.MapTileRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import mu.KotlinLogging

@ApplicationScoped
class MapTileService @Inject constructor(private val mapTileRepository: MapTileRepository) {

    private val logger = KotlinLogging.logger {}

    fun getMapTiles(x1: Int, x2: Int, y1: Int, y2: Int): List<MapTile> {
        val amountOfMapTiles = (x2 - x1) * (y2 - y1);
        val mapTiles = mapTileRepository.findMapTilesBetween(x1, x2, y1, y2).toMutableList();
        val newMapTiles = emptyList<MapTile>().toMutableList()
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

                // TODO: This is very slow... >7sec for 10_000 tiles ... maybe index?

                mapTileRepository.saveMapTiles(newMapTiles)
            }
            val t3 = System.currentTimeMillis()
            logger.info { "Saving new map tiles took ${t3 - t2}ms" }
        }
        return mapTiles
    }

}
