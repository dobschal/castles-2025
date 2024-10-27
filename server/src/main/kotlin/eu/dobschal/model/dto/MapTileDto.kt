package eu.dobschal.model.dto

import eu.dobschal.model.enum.MapTileType
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.LocalDateTime

@RegisterForReflection
class MapTileDto(
    var id: Int? = null,
    var x: Int? = null,
    var y: Int? = null,
    var type: MapTileType? = null,
    var createdAt: LocalDateTime? = null
)
