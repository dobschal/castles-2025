package eu.dobschal.model.entity

import eu.dobschal.model.enum.TutorialStatus
import eu.dobschal.model.enum.TutorialType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tutorial")
class Tutorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    lateinit var type: TutorialType

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    lateinit var status: TutorialStatus

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    var user: User? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

}
