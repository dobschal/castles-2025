package eu.dobschal.model.dto.response

import eu.dobschal.model.enum.UnitType

data class PricesResponseDto(
    val unitCreationPrices: Map<UnitType, Int>,
    val unitMovePrices: Map<UnitType, Int>
)
