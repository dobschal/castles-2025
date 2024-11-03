package eu.dobschal.model.dto

import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.UnitType
import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName
import io.quarkus.runtime.annotations.RegisterForReflection
import java.time.LocalDateTime

@RegisterForReflection
class EventDto(
    var id: Int? = null,
    var x: Int? = null,
    var y: Int? = null,
    var type: EventType? = null,
    var user1: UserDto? = null,
    var user2: UserDto? = null,
    var building: BuildingDto? = null,
    var unit: UnitDto? = null,
    var createdAt: LocalDateTime? = null
) {
    constructor(
        id: Int? = null,
        x: Int? = null,
        y: Int? = null,
        type: EventType? = null,
        @ProjectedFieldName("user1.id") user1Id: Int? = null,
        @ProjectedFieldName("user1.username") user1Username: String? = null,
        @ProjectedFieldName("user1.beer") user1Beer: Int? = null,
        @ProjectedFieldName("user1.createdAt") user1CreatedAt: LocalDateTime? = null,
        @ProjectedFieldName("user1.avatarId") user1AvatarId: Int? = null,
        @ProjectedFieldName("user2.id") user2Id: Int? = null,
        @ProjectedFieldName("user2.username") user2Username: String? = null,
        @ProjectedFieldName("user2.beer") user2Beer: Int? = null,
        @ProjectedFieldName("user2.createdAt") user2CreatedAt: LocalDateTime? = null,
        @ProjectedFieldName("user2.avatarId") user2AvatarId: Int? = null,
        @ProjectedFieldName("building.id") buildingId: Int? = null,
        @ProjectedFieldName("building.x") buildingX: Int? = null,
        @ProjectedFieldName("building.y") buildingY: Int? = null,
        @ProjectedFieldName("building.type") buildingType: BuildingType? = null,
        @ProjectedFieldName("building.level") buildingLevel: Int? = null,
        @ProjectedFieldName("building.createdAt") buildingCreatedAt: LocalDateTime? = null,
        @ProjectedFieldName("unit.id") unitId: Int? = null,
        @ProjectedFieldName("unit.x") unitX: Int? = null,
        @ProjectedFieldName("unit.y") unitY: Int? = null,
        @ProjectedFieldName("unit.type") unitType: UnitType? = null,
        @ProjectedFieldName("unit.createdAt") unitCreatedAt: LocalDateTime? = null,
        createdAt: LocalDateTime? = null,
    ) : this(id, x, y, type, null, null, null, null, createdAt) {
        user1 = UserDto(user1Id, user1Username, "", user1Beer, user1AvatarId, user1CreatedAt)
        user2 = UserDto(user2Id, user2Username, "", user2Beer, user2AvatarId, user2CreatedAt)
        building = BuildingDto(buildingId, buildingX, buildingY, buildingType, buildingLevel, null, buildingCreatedAt)
        unit = UnitDto(unitId, unitX, unitY, unitType, null, unitCreatedAt)
    }
}
