package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.User
import eu.dobschal.repository.*
import eu.dobschal.utils.hash
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class BaseResourceTest {
    val logger = KotlinLogging.logger {}

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var mapTileRepository: MapTileRepository

    @Inject
    lateinit var buildingRepository: BuildingRepository

    @Inject
    lateinit var unitRepository: UnitRepository

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var tutorialRepository: TutorialRepository

    var user1: User? = null
    var user2: User? = null
    var jwt1: String? = null
    var jwt2: String? = null

    @AfterEach
    @Transactional
    fun tearDown() {
        mapTileRepository.deleteAll()
        buildingRepository.deleteAll()
        userRepository.deleteAll()
        unitRepository.deleteAll()
        eventRepository.deleteAll()
        tutorialRepository.deleteAll()
    }

    @BeforeEach
    fun beforeEach() {
        createUsers()
        jwt1 = getJwt("user1")
        jwt2 = getJwt("user2")
    }

    @Transactional
    fun createUsers() {
        user1 = userRepository.createUser("user1", hash("password"))
        user2 = userRepository.createUser("user2", hash("password"))
    }

    fun <ResponseType> assertOkPostRequest(url: String, request: Any, responseType: Class<ResponseType>): ResponseType {
        return given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(responseType)
    }

    fun <ResponseType> assertOkGetRequest(url: String, responseType: Class<ResponseType>): ResponseType {
        return given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get(url)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(responseType)
    }

    private fun getJwt(username: String): String {
        val response = given()
            .body(UserCredentialsDto(username, "password"))
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .post("/v1/users/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(JwtResponseDto::class.java)
        return response.jwt
    }
}
