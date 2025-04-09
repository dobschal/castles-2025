package eu.dobschal.model.entity

import eu.dobschal.model.enum.MapTileType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "map_tile", uniqueConstraints = [UniqueConstraint(columnNames = ["x", "y"])])
@NamedQueries(
    NamedQuery(
        name = "MapTile.findByXAndY",
        query = "select id, x, y, type, createdAt from MapTile where x = ?1 and y = ?2"
    ),
    NamedQuery(
        name = "MapTile.findMapTilesBetween",
        query = "select id, x, y, type, createdAt from MapTile where x >= ?1 and x < ?2 and y >= ?3 and y < ?4 ORDER BY x DESC, y ASC"
    )
)
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
