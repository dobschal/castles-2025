package eu.dobschal.model.entity

import eu.dobschal.model.enum.MapTileType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "map_tile")
class MapTile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "x", nullable = false)
    var x: Int? = null

    @Column(name = "y", nullable = false)
    var y: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    lateinit var type: MapTileType

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
}
