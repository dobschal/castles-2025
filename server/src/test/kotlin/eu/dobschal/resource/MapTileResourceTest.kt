package eu.dobschal.resource

import WithDefaultUser
import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.repository.MapTileRepository
import eu.dobschal.repository.UserRepository
import eu.dobschal.utils.hash
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.core.Response
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@QuarkusTest
class MapTileResourceTest {

    private val logger = KotlinLogging.logger {}

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var mapTileRepository: MapTileRepository

    val endpoint = "/v1/map-tiles"

    @AfterEach
    @Transactional
    fun tearDown() {
        mapTileRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    @WithDefaultUser
    fun `Fetching map tiles is creating new ones at random`() {
        assert(mapTileRepository.listAll().size == 0)
        val response = given()
            .`when`()
            .get("$endpoint?x1=0&x2=10&y1=0&y2=10")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(List::class.java)
        assert(response.size == 100)
        assert(mapTileRepository.listAll().size == 100)
    }

    @Test
    @WithDefaultUser
    fun `Fetching map tiles twice is returning the same tiles`() {
        assert(mapTileRepository.listAll().size == 0)
        val response = given()
            .`when`()
            .get("$endpoint?x1=0&x2=10&y1=0&y2=10")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(List::class.java)
        assert(response.size == 100)
        assert(mapTileRepository.listAll().size == 100)
        val response2 = given()
            .`when`()
            .get("$endpoint?x1=0&x2=10&y1=0&y2=10")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(List::class.java)
        assert(response2.size == 100)
        assert(mapTileRepository.listAll().size == 100)
        assert(response == response2)
    }

    @Test
    @WithDefaultUser
    fun `Fetching a tones of new map tiles is fast`() {
        val t1 = System.currentTimeMillis()
        val response = given()
            .`when`()
            .get("$endpoint?x1=0&x2=33&y1=0&y2=33")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(List::class.java)
        val t2 = System.currentTimeMillis()
        assert(response.size == 1089)
        logger.info { "Fetching 1089 map tiles took ${t2 - t1}ms" }
        assert(t2 - t1 < 1000)
    }
}
