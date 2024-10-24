package eu.dobschal.service

import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.UnitRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlin.math.pow

@ApplicationScoped
class PriceService @Inject constructor(private val unitRepository: UnitRepository) {

    fun getPriceForUnitCreation(user: User, type: UnitType): Int {
        val price = when (type) {
            UnitType.WORKER -> 100
            UnitType.SPEARMAN -> 200
            UnitType.SWORDSMAN -> 300
            UnitType.HORSEMAN -> 400
            else -> throw IllegalArgumentException("Unknown unit type")
        }
        val units = unitRepository.findAllByUser(user.id!!)
        return (price * 2.0.pow(units.size.toDouble())).toInt()
    }

    fun getPriceForUnitMove(type: UnitType): Int {
        return when (type) {
            UnitType.WORKER -> 10
            UnitType.SPEARMAN -> 20
            UnitType.SWORDSMAN -> 30
            UnitType.HORSEMAN -> 40
            else -> throw IllegalArgumentException("Unknown unit type")
        }
    }
}
