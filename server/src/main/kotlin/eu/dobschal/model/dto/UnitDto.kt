package eu.dobschal.model.dto

import eu.dobschal.model.enum.UnitType
import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.LocalDateTime


@RegisterForReflection
class UnitDto(
    var id: Int? = null,
    var x: Int? = null,
    var y: Int? = null,
    var type: UnitType? = null,
    var user: UserDto? = null,
    var createdAt: LocalDateTime? = null
) {
    constructor(
        id: Int? = null,
        x: Int? = null,
        y: Int? = null,
        type: UnitType? = null,
        @ProjectedFieldName("user.id") userId: Int? = null,
        @ProjectedFieldName("user.username") userUsername: String? = null,
        @ProjectedFieldName("user.beer") userBeer: Int? = null,
        @ProjectedFieldName("user.avatarId") avatarId: Int? = null,
        @ProjectedFieldName("user.createdAt") userCreatedAt: LocalDateTime? = null,
        createdAt: LocalDateTime? = null
    ) : this(id, x, y, type, null, createdAt) {
        user = UserDto(userId, userUsername, "", userBeer, avatarId, userCreatedAt)
    }
}
