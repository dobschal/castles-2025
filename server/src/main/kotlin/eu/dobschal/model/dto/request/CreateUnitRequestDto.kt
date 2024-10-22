package eu.dobschal.model.dto.request

import eu.dobschal.model.enum.UnitType

data class CreateUnitRequestDto(
    val x: Int,
    val y: Int,
    val type: UnitType
)
