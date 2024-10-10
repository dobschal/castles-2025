package eu.dobschal.service

import eu.dobschal.model.entity.MapTile
import eu.dobschal.repository.MapTileRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class MapTileService @Inject constructor(private val mapTileRepository: MapTileRepository) {

    fun getMapTiles(x1: Int, x2: Int, y1: Int, y2: Int): List<MapTile> {
        val amountOfMapTiles = (x2 - x1) * (y2 - y1);
        val mapTiles = mapTileRepository.findMapTilesBetween(x1, x2, y1, y2).toMutableList();
        if (mapTiles.size < amountOfMapTiles) {
            for (x in x1 until x2) {
                for (y in y1 until y2) {
                    if (!mapTileRepository.mapTileExists(x, y)) {
                        mapTiles.add(mapTileRepository.createMapTile(x, y))
                    }
                }
            }
        }
        return mapTiles
    }

}
