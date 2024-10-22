package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.MapTileRepository
import eu.dobschal.repository.UnitRepository
import eu.dobschal.repository.UserRepository
import eu.dobschal.utils.hash
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


@QuarkusTest
class UnitResourceTest {

    private val logger = KotlinLogging.logger {}

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var mapTileRepository: MapTileRepository

    @Inject
    private lateinit var buildingRepository: BuildingRepository

    @Inject
    private lateinit var unitRepository: UnitRepository

    val endpoint = "/v1/units"

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

    fun getJwt(username: String): String {
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

    @Test
    fun `Get units from x1, y1, x2,y2 returns right amount of correct units`() {
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
            .get("$endpoint?x1=0&x2=2&y1=0&y2=2")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(List::class.java)
        assert(response.size == 1)
    }

    @Test
    fun `Get units from x1, y1, x2,y2 without units in that area is returning an empty list`() {
        TODO()
    }

    @Test
    fun `Create unit on wrong building fails`() {
        TODO()
    }

    @Test
    fun `Create unit without enough resources (beer) fails`() {
        TODO()
    }

    @Test
    fun `Create unit adds correct unit to database`() {
        TODO()
    }

    @Test
    fun `Create unit adds correct unit to database and is return by get units`() {
        TODO()
    }

    @Test
    fun `Create unit on field with existing unit fails`() {
        TODO()
    }

    @Test
    fun `Create worker on village of opponent fails`() {
        TODO()
    }
}
