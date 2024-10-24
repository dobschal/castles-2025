package eu.dobschal.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "castles_user")
class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "username", nullable = false, unique = true)
    lateinit var username: String

    @Column(name = "password", nullable = false)
    lateinit var password: String

    @Column(name = "beer", nullable = false)
    var beer: Int? = null

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

}
