package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.dto.request.CreateUnitRequestDto
import eu.dobschal.model.dto.request.MoveUnitRequestDto
import eu.dobschal.model.dto.request.SaveStartVillageRequestDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.*
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
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
import kotlin.Array
import kotlin.String
import kotlin.apply
import kotlin.assert


@QuarkusTest
class EventResourceTest {

    private val logger = KotlinLogging.logger {}

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var mapTileRepository: MapTileRepository

    @Inject
    private lateinit var buildingRepository: BuildingRepository

    @Inject
    private lateinit var unitRepository: UnitRepository

    @Inject
    private lateinit var eventRepository: EventRepository

    val endpoint = "/v1/events"

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
    fun `Event is added after unit move`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("/v1/units/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        assert(eventRepository.listAll().first().x == 3)
        assert(eventRepository.listAll().first().y == 3)
        assert(eventRepository.listAll().first().type == EventType.UNIT_MOVED)
    }

    @Test
    fun `Event is added after unit creation`() {
        val village = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("/v1/units")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Unit::class.java)
        assert(eventRepository.listAll().first().x == 1)
        assert(eventRepository.listAll().first().y == 1)
        assert(eventRepository.listAll().first().type == EventType.UNIT_CREATED)
    }

    @Test
    fun `Event is added after start village creation`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("/v1/buildings/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(eventRepository.listAll().first().x == 1)
        assert(eventRepository.listAll().first().y == 1)
        assert(eventRepository.listAll().first().type == EventType.BUILDING_CREATED)
    }

    @Test
    fun `Event API is returning events`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("/v1/buildings/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(eventRepository.listAll().first().x == 1)
        assert(eventRepository.listAll().first().y == 1)
        assert(eventRepository.listAll().first().type == EventType.BUILDING_CREATED)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/events?x1=0&y1=0&x2=4&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<Event>::class.java)
        assert(response.size == 1)
        assert(response[0].x == 1)
        assert(response[0].y == 1)
        assert(response[0].type == EventType.BUILDING_CREATED)
    }

    @Test
    fun `Event API is not returning events that aren't in the requested range`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = SaveStartVillageRequestDto(1, 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("/v1/buildings/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(eventRepository.listAll().first().x == 1)
        assert(eventRepository.listAll().first().y == 1)
        assert(eventRepository.listAll().first().type == EventType.BUILDING_CREATED)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("/v1/events?x1=2&y1=2&x2=4&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<Event>::class.java)
        assert(response.size == 0)
    }
}
