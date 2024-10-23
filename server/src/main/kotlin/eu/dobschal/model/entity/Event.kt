package eu.dobschal.model.entity

import eu.dobschal.model.enum.EventType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "event")
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
