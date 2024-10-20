package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.dto.request.SaveStartVillageRequestDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.MapTileRepository
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
import java.time.LocalDateTime

@QuarkusTest
class BuildingResourceTest {

    private val logger = KotlinLogging.logger {}

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var mapTileRepository: MapTileRepository

    @Inject
    private lateinit var buildingRepository: BuildingRepository

    val endpoint = "/v1/buildings"

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
    fun `Getting start village w_o any building returns 404`() {
        assert(buildingRepository.listAll().isEmpty())
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    fun `Getting start village with only other buildings in the database returns 404`() {
        val village = Building().apply {
            x = 1
            y = 1
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        assert(buildingRepository.listAll().size == 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    fun `Getting start village with buildings, returns the oldest one`() {
        val village1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
            createdAt = LocalDateTime.now()
        }
        buildingRepository.save(village1)
        val village2 = Building().apply {
            x = 2
            y = 2
            user = user1
            type = BuildingType.VILLAGE
            createdAt = LocalDateTime.now().plusMonths(1)
        }
        buildingRepository.save(village2)
        assert(buildingRepository.listAll().size == 2)
        val village = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Building::class.java)
        assert(village.x == village1.x)
        assert(village.y == village1.y)
    }

    @Test
    fun `Save start village with buildings too close fails`() {
        val farm = Building().apply {
            x = 1
            y = 1
            user = user2
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val request = SaveStartVillageRequestDto(2, 3)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
    }

    @Test
    fun `Save start village on wrong tile type fails`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.WATER
        }
        mapTileRepository.saveMapTiles(listOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().isEmpty())
    }

    @Test
    fun `Save start village works`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(listOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 1)
    }

    @Test
    fun `Save start village with already having a village, fails`() {
        val village1 = Building().apply {
            x = 50
            y = 50
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village1)
        assert(buildingRepository.listAll().size == 1)
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(listOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
    }

    @Test
    fun `Get buildings works`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(listOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=0&x2=2&y1=0&y2=2")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<Building>::class.java)
        assert(response.size == 1)
    }

    @Test
    fun `Get buildings returns empty list when no building in range`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(listOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=2&x2=10&y1=2&y2=50")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<Building>::class.java)
        assert(response.size == 0)
    }


}
