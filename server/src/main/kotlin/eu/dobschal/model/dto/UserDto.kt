package eu.dobschal.model.dto

import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.LocalDateTime

@RegisterForReflection
class UserDto(
    var id: Int? = null,
    var username: String? = null,
    var password: String? = null,
    var beer: Int? = null,
    val avatarId: Int? = null,
    var createdAt: LocalDateTime? = null
)
