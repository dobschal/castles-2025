package eu.dobschal.model.entity

import eu.dobschal.model.enum.EventType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event")
@NamedQueries(
    NamedQuery(
        name = "Event.findEventsBetween",
        query = "select e.id, e.x, e.y, e.type, user1.id, user1.username, user1.beer, user1.createdAt, user1.gold, user1.avatarId, user2.id, user2.username, user2.beer, user2.createdAt, user2.gold, user2.avatarId, building.id, building.x, building.y, building.type, building.level, building.createdAt, unit.id, unit.x, unit.y, unit.type, unit.createdAt, e.createdAt from Event e left join User user1 on user1.id = e.user1.id left join User user2 on user2.id = e.user2.id left join Building building on building.id = e.building.id left join Unit unit on unit.id = e.unit.id where e.x >= ?1 and e.x < ?2 and e.y >= ?3 and e.y < ?4 and e.id > ?5 ORDER BY e.id DESC LIMIT 500"
    )
)
class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "x", nullable = false)
    var x: Int? = null

    @Column(name = "y", nullable = false)
    var y: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    lateinit var type: EventType

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = true)
    var user1: User? = null

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = true)
    var user2: User? = null

    @ManyToOne
    @JoinColumn(name = "building_id", nullable = true)
    var building: Building? = null

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = true)
    var unit: Unit? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

}
