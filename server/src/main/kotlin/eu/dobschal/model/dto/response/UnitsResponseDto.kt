package eu.dobschal.model.dto.response

import eu.dobschal.model.dto.UnitDto
import eu.dobschal.utils.*

data class UnitsResponseDto(
    val units: List<UnitDto>,
    val workerMovesPerHour: Int = WORKER_MOVES_PER_HOUR,
    val spearmanMovesPerHour: Int = SPEARMAN_MOVES_PER_HOUR,
    val swordsmanMovesPerHour: Int = SWORDSMAN_MOVES_PER_HOUR,
    val horsemanMovesPerHour: Int = HORSEMAN_MOVES_PER_HOUR,
    val dragonMovesPerHour: Int = DRAGON_MOVES_PER_HOUR,
    val archerMovesPerHour: Int = ARCHER_MOVES_PER_HOUR,
    val unitsLimit: Int,
    val unitsCount: Int
)
