package eu.dobschal.model.dto.response

import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.UnitType

data class PricesResponseDto(
    val unitCreationPrices: Map<UnitType, Int>,
    val unitMovePrices: Map<UnitType, Int>,
    val buildingPrices: Map<BuildingType, Int>,
    val buildingLevelUpPrices: Map<BuildingType, Int>,
    val sellBeerPrice: Int
)
