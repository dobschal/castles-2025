package eu.dobschal.resource

import WithDefaultUser
import eu.dobschal.model.dto.response.PricesResponseDto
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.UnitType
import eu.dobschal.utils.SELL_BEER_PRICE
import eu.dobschal.utils.SWORDSMAN_BASE_PRICE
import eu.dobschal.utils.UNIT_PRICE_FACTOR
import eu.dobschal.utils.WORKER_BASE_PRICE
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test

@QuarkusTest
class PriceResourceTest : BaseResourceTest() {
    @Test
    @WithDefaultUser
    fun `Prices are correct with 1 unit`() {
        val unit = Unit().apply {
            x = 1
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        assert(unitRepository.listAll().size == 1)
        val response = given()
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        assert(response.unitCreationPrices.get(UnitType.WORKER) == (WORKER_BASE_PRICE * UNIT_PRICE_FACTOR).toInt())
        assert(response.unitCreationPrices.get(UnitType.SWORDSMAN) == (SWORDSMAN_BASE_PRICE * UNIT_PRICE_FACTOR).toInt())
    }

    @Test
    @WithDefaultUser
    fun `Prices are correct with 3 units`() {
        val unit1 = Unit().apply {
            x = 1
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit1)
        val unit2 = Unit().apply {
            x = 2
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
        val unit3 = Unit().apply {
            x = 3
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit3)
        assert(unitRepository.listAll().size == 3)
        val response = assertOkGetRequest("/v1/prices", PricesResponseDto::class.java)
        logger.info { response }
        assert(response.unitCreationPrices.get(UnitType.WORKER) == (WORKER_BASE_PRICE * UNIT_PRICE_FACTOR * UNIT_PRICE_FACTOR * UNIT_PRICE_FACTOR).toInt())
        assert(response.unitCreationPrices.get(UnitType.SWORDSMAN) == (SWORDSMAN_BASE_PRICE * UNIT_PRICE_FACTOR * UNIT_PRICE_FACTOR * UNIT_PRICE_FACTOR).toInt())
    }

    @Test
    @WithDefaultUser
    fun `Gold price for beer is returned in prices response`() {
        val response = assertOkGetRequest("/v1/prices", PricesResponseDto::class.java)
        assert(response.sellBeerPrice == SELL_BEER_PRICE)
    }
}
