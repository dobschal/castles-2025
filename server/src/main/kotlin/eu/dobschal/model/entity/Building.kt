package eu.dobschal.model.entity

import eu.dobschal.model.enum.BuildingType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "building")
@NamedQueries(
    NamedQuery(
        name = "Building.findAllByUser",
        query = "select id, x, y, type, level, user.id, user.username, user.beer, user.createdAt, createdAt from Building b where b.user.id = ?1"
    ),
    NamedQuery(
        name = "Building.findBuildingsBetween",
        query = "select id, x, y, type, level, user.id, user.username, user.beer, user.createdAt, createdAt from Building b where b.x >= ?1 and b.x < ?2 and b.y >= ?3 and b.y < ?4"
    )
)
class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "x", nullable = false)
    var x: Int? = null

    @Column(name = "y", nullable = false)
    var y: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    lateinit var type: BuildingType

    @Column(name = "level", nullable = false)
    var level: Int? = 1

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    var user: User? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

}
