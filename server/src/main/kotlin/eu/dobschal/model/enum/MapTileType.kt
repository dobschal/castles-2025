package eu.dobschal.model.enum

enum class MapTileType {
    PLAIN,
    FOREST,
    MOUNTAIN,
    WATER;

    companion object {

        fun pickOneRandomly(): MapTileType {
            return entries.toTypedArray().random()
        }
    }
}
