package eu.dobschal.model.dto.response

import eu.dobschal.model.dto.UnitDto
import eu.dobschal.utils.HORSEMAN_MOVES_PER_HOUR
import eu.dobschal.utils.SPEARMAN_MOVES_PER_HOUR
import eu.dobschal.utils.SWORDSMAN_MOVES_PER_HOUR
import eu.dobschal.utils.WORKER_MOVES_PER_HOUR

data class UnitsResponseDto(
    val units: List<UnitDto>,
    val workerMovesPerHour: Int = WORKER_MOVES_PER_HOUR,
    val spearmanMovesPerHour: Int = SPEARMAN_MOVES_PER_HOUR,
    val swordsmanMovesPerHour: Int = SWORDSMAN_MOVES_PER_HOUR,
    val horsemanMovesPerHour: Int = HORSEMAN_MOVES_PER_HOUR
)
