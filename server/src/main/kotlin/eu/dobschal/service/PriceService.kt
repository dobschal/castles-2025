package eu.dobschal.service

import eu.dobschal.model.dto.response.PricesResponseDto
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.UnitRepository
import eu.dobschal.utils.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlin.math.pow

@ApplicationScoped
class PriceService @Inject constructor(
    private val unitRepository: UnitRepository,
    private val userService: UserService
) {

    fun getPriceForUnitCreation(user: User, type: UnitType): Int {
        val price = when (type) {
            UnitType.WORKER -> WORKER_BASE_PRICE
            UnitType.SPEARMAN -> SPEARMAN_BASE_PRICE
            UnitType.SWORDSMAN -> SWORDSMAN_BASE_PRICE
            UnitType.HORSEMAN -> HORSEMAN_BASE_PRICE
        }
        val units = unitRepository.findAllByUser(user.id!!)
        return (price * 2.0.pow(units.size.toDouble())).toInt()
    }

    fun getPriceForUnitMove(type: UnitType): Int {
        return when (type) {
            UnitType.WORKER -> WORKER_MOVE_PRICE
            UnitType.SPEARMAN -> SPEARMAN_MOVE_PRICE
            UnitType.SWORDSMAN -> SWORDSMAN_MOVE_PRICE
            UnitType.HORSEMAN -> HORSEMAN_MOVE_PRICE
        }
    }

    fun getAllPrices(): PricesResponseDto {
        val user = userService.getCurrentUser()
        val unitCreationPrices = UnitType.entries.associateWith { getPriceForUnitCreation(user, it) }
        val unitMovePrices = UnitType.entries.associateWith { getPriceForUnitMove(it) }
        return PricesResponseDto(
            unitCreationPrices,
            unitMovePrices
        )
    }
}
