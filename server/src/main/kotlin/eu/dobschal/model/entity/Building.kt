package eu.dobschal.model.entity

import eu.dobschal.model.enum.BuildingType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "building")
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

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_id", nullable = true)
    var user: User? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

}
