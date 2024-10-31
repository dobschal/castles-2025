package eu.dobschal.resource

import eu.dobschal.model.dto.request.SetTutorialStatusRequestDto
import eu.dobschal.model.dto.response.ErrorResponseDto
import eu.dobschal.model.entity.Tutorial
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.TutorialStatus
import eu.dobschal.model.enum.TutorialType
import eu.dobschal.model.enum.UnitType
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test

@QuarkusTest
class TutorialResourceTest : BaseResourceTest() {

    @Test
    fun `Getting next tutorial works`() {
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/tutorials/next")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Tutorial::class.java)
        assert(response.status == TutorialStatus.OPEN)
        assert(response.type == TutorialType.FIRST_WORKER)
    }

    @Test
    fun `Set tutorial status works`() {
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/tutorials/next")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Tutorial::class.java)
        assert(response.status == TutorialStatus.OPEN)
        assert(response.type == TutorialType.FIRST_WORKER)
        val request = SetTutorialStatusRequestDto(TutorialStatus.COMPLETED, response.id!!)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("/v1/tutorials")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(tutorialRepository.findById(response.id!!)?.status == TutorialStatus.COMPLETED)
    }

    @Test
    fun `Get next tutorial works when one is solved`() {
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/tutorials/next")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Tutorial::class.java)
        assert(response.status == TutorialStatus.OPEN)
        assert(response.type == TutorialType.FIRST_WORKER)
        val request = SetTutorialStatusRequestDto(TutorialStatus.COMPLETED, response.id!!)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("/v1/tutorials")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(tutorialRepository.findById(response.id!!)?.status == TutorialStatus.COMPLETED)
        val response2 = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/tutorials/next")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Tutorial::class.java)
        assert(response2.status == TutorialStatus.OPEN)
        assert(response2.type == TutorialType.FIRST_FARM)
    }

    @Test
    fun `Get next tutorial returning 404 if non existing anymore`() {
        TutorialType.entries.forEach {
            tutorialRepository.persist(Tutorial().apply {
                user = user1
                type = it
                status = TutorialStatus.COMPLETED
            })
        }
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/tutorials/next")
            .then()
            .statusCode(Response.Status.NOT_FOUND.statusCode)
            .extract().`as`(ErrorResponseDto::class.java)
        assert(response.message == "serverError.noMoreTutorials")
    }

    @Test
    fun `Get next tutorial return status open, solved OR collected, user needs to collect it manually`() {
        val unit = Unit().apply {
            x = 1
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/tutorials/next")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Tutorial::class.java)
        assert(response.status == TutorialStatus.CAN_BE_COMPLETED)
        assert(response.type == TutorialType.FIRST_WORKER)
    }
}
