package eu.dobschal.model.entity

import eu.dobschal.model.enum.UnitType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "unit")
@NamedQueries(
    NamedQuery(
        name = "Unit.findAllByUser",
        query = "select id, x, y, type, user.id, user.username, user.beer, user.gold, user.avatarId, user.createdAt, createdAt from Unit u where u.user.id = ?1"
    ),
    NamedQuery(
        name = "Unit.findUnitsBetween",
        query = "select id, x, y, type, user.id, user.username, user.beer, user.gold, user.avatarId, user.createdAt, createdAt from Unit u where u.x >= ?1 and u.x < ?2 and u.y >= ?3 and u.y < ?4"
    )
)
class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "x", nullable = false)
    var x: Int? = null

    @Column(name = "y", nullable = false)
    var y: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    lateinit var type: UnitType

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    var user: User? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

}
