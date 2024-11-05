package eu.dobschal.model.dto.response

import eu.dobschal.model.dto.BuildingDto

data class BuildingsResponseDto(
    val buildings: List<BuildingDto>,
    val breweryBeerProductionPerHour: Int,
    val breweryBeerStorage: Int,
    val totalBeerStorage: Int,
    val amountOfVillages: Int,
    val totalGoldStorage: Int
)
