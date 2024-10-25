package eu.dobschal.resource

import eu.dobschal.model.dto.request.SaveStartVillageRequestDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.MapTileType
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@QuarkusTest
class BuildingResourceTest : BaseResourceTest() {

    private val endpoint = "/v1/buildings"

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
        mapTileRepository.saveMapTiles(setOf(mapTile))
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
        mapTileRepository.saveMapTiles(setOf(mapTile))
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
        mapTileRepository.saveMapTiles(setOf(mapTile))
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
        mapTileRepository.saveMapTiles(setOf(mapTile))
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
        mapTileRepository.saveMapTiles(setOf(mapTile))
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

    @Test
    fun `Create building should work`() {
        TODO()
    }

    @Test
    fun `Create building not on PLAIN should fail`() {
        TODO()
    }

    @Test
    fun `Create building without enough beer should fail`() {
        TODO()
    }

    @Test
    fun `Create building on other building should fail`() {
        TODO()
    }

    @Test
    fun `Create brewery without farm next to it should fail`() {
        TODO()
    }

    @Test
    fun `Create building without worker on field should fail`() {
        TODO()
    }

    @Test
    fun `Create building should deduct beer from user`() {
        TODO()
    }

    @Test
    fun `Worker dies after creating a building`() {
        TODO()
    }


}
