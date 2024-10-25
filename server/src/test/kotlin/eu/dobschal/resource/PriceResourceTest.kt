package eu.dobschal.resource

import eu.dobschal.model.dto.response.PricesResponseDto
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.UnitType
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test

@QuarkusTest
class PriceResourceTest : BaseResourceTest() {
    @Test
    fun `Prices are corret with 1 unit`() {
        val unit = Unit().apply {
            x = 1
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        assert(unitRepository.listAll().size == 1)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        assert(response.unitCreationPrices.get(UnitType.WORKER) == 200)
        assert(response.unitCreationPrices.get(UnitType.SWORDSMAN) == 600)
    }

    @Test
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
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        logger.info { response }
        assert(response.unitCreationPrices.get(UnitType.WORKER) == 800)
        assert(response.unitCreationPrices.get(UnitType.SWORDSMAN) == 2400)
    }
}
