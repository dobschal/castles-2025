package eu.dobschal.model.dto.request

import eu.dobschal.model.enum.BuildingType

data class CreateBuildingRequestDto(
    val x: Int,
    val y: Int,
    val type: BuildingType
)
