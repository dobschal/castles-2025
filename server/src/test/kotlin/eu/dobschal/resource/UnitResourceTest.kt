package eu.dobschal.resource

import eu.dobschal.model.dto.request.CreateUnitRequestDto
import eu.dobschal.model.dto.request.MoveUnitRequestDto
import eu.dobschal.model.dto.response.ErrorResponseDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.utils.START_BEER
import eu.dobschal.utils.WORKER_BASE_PRICE
import eu.dobschal.utils.WORKER_MOVE_PRICE
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test


@QuarkusTest
class UnitResourceTest : BaseResourceTest() {

    private val endpoint = "/v1/units"

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
            .get("$endpoint?x1=2&x2=4&y1=2&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(List::class.java)
        assert(response.size == 0)
    }

    @Test
    fun `Create unit on wrong building fails`() {
        val castle = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().`as`(ErrorResponseDto::class.java)
        assert(response.message == "serverError.wrongBuildingType")
    }

    @Test
    fun `Create unit adds correct unit to database and is return by get units`() {
        val village = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Unit::class.java)
        assert(response.type == UnitType.WORKER)
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.WORKER)
        val response2 = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=0&x2=4&y1=0&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<Unit>::class.java)
        assert(response2.size == 1)
        assert(response2.first().type == UnitType.WORKER)

    }

    @Test
    fun `Create unit on field with existing unit fails`() {
        val village = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val unit = Unit().apply {
            x = 1
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        assert(unitRepository.listAll().size == 1)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().`as`(ErrorResponseDto::class.java)
        logger.info { "Response: $response" }
        assert(response.message == "serverError.conflictingUnit")
        assert(unitRepository.listAll().size == 1)
    }

    @Test
    fun `Create worker on village of opponent fails`() {
        val village = Building().apply {
            x = 1
            y = 1
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        assert(unitRepository.listAll().size == 0)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().`as`(ErrorResponseDto::class.java)
        assert(response.message == "serverError.wrongBuildingOwner")
        assert(unitRepository.listAll().size == 0)
    }

    @Test
    fun `Move unit to water should fail`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.WATER
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
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(unitRepository.listAll().first().x == 2)
    }

    @Test
    fun `Move unit to PLAIN, FOREST or MOUNTAIN should work`() {
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
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unitRepository.listAll().first().x == 3)
        assert(unitRepository.listAll().first().y == 3)
    }

    @Test
    fun `Move unit to field with existing own unit should fail`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
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
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    fun `Move unit to field with existing non own unit should work (fight)`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user2
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.SWORDSMAN
        }
        unitRepository.save(unit)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
    }

    @Test
    fun `Move non owned unit should fail`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user2
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    fun `Move owned unit to same field should fail`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit = Unit().apply {
            x = 3
            y = 3
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
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    fun `Move owned unit further than 1 field should fail`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit = Unit().apply {
            x = 1
            y = 1
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
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    fun `Move unit without enough beer should fail`() {
        userRepository.deductBeerFromUser(user1!!.id!!, START_BEER - WORKER_MOVE_PRICE + 1)
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
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unitRepository.listAll().first().x == 2)
        assert(unitRepository.listAll().first().y == 2)
    }

    @Test
    fun `Create unit without enough beer should fail`() {
        userRepository.deductBeerFromUser(user1!!.id!!, START_BEER - WORKER_BASE_PRICE + 1)
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
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(unitRepository.listAll().size == 0)
        val response2 = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=0&x2=4&y1=0&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<Unit>::class.java)
        assert(response2.size == 0)
    }

    @Test
    fun `Create unit should deduct beer from user`() {
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
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Unit::class.java)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER - WORKER_BASE_PRICE)
    }

    @Test
    fun `Move unit should deduct beer from user`() {
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
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER - WORKER_MOVE_PRICE)
    }

    @Test
    fun `Move worker unit to opponent building should fail`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val farm = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unitRepository.listAll().first().x == 2)
        assert(unitRepository.listAll().first().y == 2)
    }

    @Test
    fun `Move worker unit to own building should work`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val farm = Building().apply {
            x = 3
            y = 3
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unitRepository.listAll().first().x == 3)
        assert(unitRepository.listAll().first().y == 3)
    }

    @Test
    fun `Move worker unit to field with opponent unit should fail`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.MOUNTAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user2
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unit.x == 2)
        assert(unit.y == 2)
    }

    @Test
    fun `A unit has a limited amount of moves per hour`() {
        TODO()
    }

    @Test
    fun `Amount of units is limited by amount of castles and their level`() {
        TODO()
    }


}
