package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.dto.response.ErrorResponseDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.User
import eu.dobschal.repository.*
import eu.dobschal.utils.hash
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.specification.RequestSpecification
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
        setBaseHeader()

        createUsers()
    }

    @Transactional
    fun createUsers() {
        user1 = userRepository.createUser(USER1, hash("password"))
        user2 = userRepository.createUser(USER2, hash("password"))
    }

    private fun setBaseHeader() {
        val requestSpec: RequestSpecification = RequestSpecBuilder()
            .addHeader("Content-Type", MediaType.APPLICATION_JSON)
            .build()

        // Set it as the global request specification
        RestAssured.requestSpecification = requestSpec
    }

    fun <ResponseType> assertOkPostRequest(url: String, request: Any, responseType: Class<ResponseType>): ResponseType {
        return given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(responseType)
    }

    fun assertBadPostRequest(
        url: String,
        request: Any
    ): ErrorResponseDto {
        return given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post(url)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().`as`(ErrorResponseDto::class.java)
    }

    fun <ResponseType> assertOkGetRequest(url: String, responseType: Class<ResponseType>): ResponseType {
        return given()
            .`when`()
            .get(url)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(responseType)
    }

    fun getJwt(username: String): String {
        val response = given()
            .body(UserCredentialsDto(username, "password"))
            .`when`()
            .post("/v1/users/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(JwtResponseDto::class.java)
        return response.jwt
    }

    companion object {
        const val USER1 = "user1"
        const val USER2 = "user2"
    }
}
