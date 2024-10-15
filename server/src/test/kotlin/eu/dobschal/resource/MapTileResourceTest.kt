package eu.dobschal.resource

import eu.dobschal.repository.MapTileRepository
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

@QuarkusTest
class MapTileResourceTest {

    private val logger = KotlinLogging.logger {}

    @Inject
    private lateinit var mapTileRepository: MapTileRepository

    val endpoint = "/v1/map-tiles"

    @AfterEach
    @Transactional
    fun tearDown() {
        mapTileRepository.deleteAll()
    }

    @Test
    fun `Fetching map tiles is creating new ones at random`() {

        // TODO: Add helper to get JWT

        TODO()

//        userRepository.createUser("existing-user", hash("password"))
//        val jwt = given()
//            .body(UserCredentialsDto("existing-user", "password"))
//            .header("Content-Type", MediaType.APPLICATION_JSON)
//            .`when`()
//            .post("$endpoint/login")
//            .then()
//            .statusCode(Response.Status.OK.statusCode)
//            .extract()
//            .asString()
//
//        val jwt = given()
//            .header("Content-Type", MediaType.APPLICATION_JSON)
//            .`when`()
//            .post("$endpoint/login")
//            .then()
//            .statusCode(Response.Status.OK.statusCode)
//            .extract()
//            .asString()
    }

    @Test
    fun `Fetching map tiles twice is returning the same tiles`() {
        TODO()
    }

    @Test
    fun `Fetching a tones of new map tiles is fast`() {
        TODO()
    }
}
