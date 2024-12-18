package eu.dobschal.resource

import WithDefaultUser
import eu.dobschal.model.dto.request.CreateUnitRequestDto
import eu.dobschal.model.dto.request.MoveUnitRequestDto
import eu.dobschal.model.dto.response.ErrorResponseDto
import eu.dobschal.model.dto.response.UnitsResponseDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.Event
import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.EventType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.utils.*
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test
import java.time.LocalDateTime


@QuarkusTest
class UnitResourceTest : BaseResourceTest() {

    private val endpoint = "/v1/units"

    @Test
    @WithDefaultUser
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
            .`when`()
            .get("$endpoint?x1=0&x2=2&y1=0&y2=2")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UnitsResponseDto::class.java)
        assert(response.units.size == 1)
    }

    @Test
    @WithDefaultUser
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
            .`when`()
            .get("$endpoint?x1=2&x2=4&y1=2&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UnitsResponseDto::class.java)
        assert(response.units.size == 0)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().`as`(ErrorResponseDto::class.java)
        assert(response.message == "serverError.wrongBuildingType")
    }

    @Test
    @WithDefaultUser
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
            .`when`()
            .get("$endpoint?x1=0&x2=4&y1=0&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UnitsResponseDto::class.java)
        assert(response2.units.size == 1)
        assert(response2.units.first().type == UnitType.WORKER)

    }

    @Test
    @WithDefaultUser
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
    @WithDefaultUser
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
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(unitRepository.listAll().first().x == 2)
    }

    @Test
    @WithDefaultUser
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
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @WithDefaultUser
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
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(unitRepository.listAll().size == 0)
        val response2 = given()
            .`when`()
            .get("$endpoint?x1=0&x2=4&y1=0&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UnitsResponseDto::class.java)
        assert(response2.units.size == 0)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Unit::class.java)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER - WORKER_BASE_PRICE)
    }

    @Test
    @WithDefaultUser
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
    @WithDefaultUser
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
    @WithDefaultUser
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
    @WithDefaultUser
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
    @WithDefaultUser
    fun `A unit has a limited amount of moves per hour`() {
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
            type = UnitType.HORSEMAN
        }
        unitRepository.save(unit)
        eventRepository.save(Event().apply {
            user1 = user1
            type = EventType.UNIT_MOVED
            this.unit = unit
            x = 12
            y = 12
            createdAt = LocalDateTime.now().minusMinutes(30)
        })
        eventRepository.save(Event().apply {
            user1 = user1
            type = EventType.UNIT_MOVED
            this.unit = unit
            x = 12
            y = 13
            createdAt = LocalDateTime.now().minusMinutes(59)
        })
        eventRepository.save(Event().apply {
            user1 = user1
            type = EventType.UNIT_MOVED
            this.unit = unit
            x = 12
            y = 14
            createdAt = LocalDateTime.now().minusMinutes(1)
        })
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        val response = given()
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
    @WithDefaultUser
    fun `One can have 2 units only without a castle`() {
        val unit1 = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.HORSEMAN
        }
        unitRepository.save(unit1)
        val unit2 = Unit().apply {
            x = 3
            y = 2
            user = user1
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit2)
        val village = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val request = CreateUnitRequestDto(1, 1, UnitType.SPEARMAN)
        given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
        assert(unitRepository.listAll().size == 2)
    }

    @Test
    @WithDefaultUser
    fun `Amount of units is limited by amount of castles and their level`() {
        val unit1 = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit1)
        val unit2 = Unit().apply {
            x = 3
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
        val village = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val castle = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER - (WORKER_BASE_PRICE * UNIT_PRICE_FACTOR * UNIT_PRICE_FACTOR).toInt())
        assert(unitRepository.listAll().size == 3)
    }


    @Test
    @WithDefaultUser
    fun `Fight units can only be build on castles`() {
        val castle = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val request = CreateUnitRequestDto(1, 1, UnitType.HORSEMAN)
        assert(unitRepository.listAll().size == 0)
        given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 1)
        val village = Building().apply {
            x = 1
            y = 2
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val request2 = CreateUnitRequestDto(1, 2, UnitType.HORSEMAN)
        given()
            .body(request2)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(unitRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
    fun `Fight units can not move over each other if from same player`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user1
            type = UnitType.HORSEMAN
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
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @WithDefaultUser
    fun `Fight units can move over other units (fight, one unit gets destroyed)`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user2
            type = UnitType.HORSEMAN
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
        assert(unitRepository.listAll().size == 2)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.HORSEMAN)
        assert(eventRepository.listAll().size == 2)
        assert(eventRepository.listAll().last().type == EventType.LOST_UNIT)
    }

    @Test
    @WithDefaultUser
    fun `Farms and Breweries are getting destroyed on conquer`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user2
            type = UnitType.HORSEMAN
        }
        unitRepository.save(unit2)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit)
        val farm = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        assert(unitRepository.listAll().size == 2)
        assert(buildingRepository.listAll().size == 1)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.SPEARMAN)
        assert(eventRepository.listAll().size == 3)
        assert(buildingRepository.listAll().size == 0)
    }

    @Test
    @WithDefaultUser
    fun `Own Farms and Breweries are not getting destroyed`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit)
        val farm = Building().apply {
            x = 3
            y = 3
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        assert(unitRepository.listAll().size == 1)
        assert(buildingRepository.listAll().size == 1)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 1)
        assert(eventRepository.listAll().size == 1)
        assert(buildingRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
    fun `Villages are getting conquered when other unit is moving onto`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user2
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit2)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.SWORDSMAN
        }
        unitRepository.save(unit)
        val village2 = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village2)
        val village = Building().apply {
            x = 3
            y = 50
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        assert(unitRepository.listAll().size == 2)
        assert(buildingRepository.listAll().size == 2)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.SWORDSMAN)
        assert(eventRepository.listAll().size == 3)
        assert(buildingRepository.listAll().size == 2)
        assert(buildingRepository.listAll().last().user?.id == user1?.id)
    }

    @Test
    @WithDefaultUser
    fun `If loosing the last village, destroy all other buildings and give user START BEER`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 4
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user2
            type = UnitType.HORSEMAN
        }
        unitRepository.save(unit2)
        val unit = Unit().apply {
            x = 3
            y = 5
            user = user1
            type = UnitType.SWORDSMAN
        }
        unitRepository.save(unit)
        val castle = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val village = Building().apply {
            x = 3
            y = 4
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        userRepository.setBeerTo(user2!!.id!!, 10)

        assert(unitRepository.listAll().size == 2)
        assert(buildingRepository.listAll().size == 2)
        assert(userRepository.findById(user2!!.id!!)!!.beer == 10)

        val request = MoveUnitRequestDto(3, 4, unit.id!!)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.SWORDSMAN)
        assert(eventRepository.listAll().size == 3)
        assert(buildingRepository.listAll().size == 1)
        assert(buildingRepository.listAll().first().user?.id == user1?.id)
        assert(userRepository.findById(user2!!.id!!)!!.beer == START_BEER)
        assert(buildingRepository.listAll().first().type == BuildingType.VILLAGE)
        assert(eventRepository.listAll().last().type == EventType.GAME_OVER)
    }

    @Test
    @WithDefaultUser
    fun `Deleting own unit works`() {
        val unit = Unit().apply {
            x = 3
            y = 5
            user = user1
            type = UnitType.SWORDSMAN
        }
        unitRepository.save(unit)

        val userUnitsBefore = unitRepository.findAllByUser(user1?.id!!).size

        given()
            .delete("$endpoint/${unit.id}")
            .then()
            .statusCode(Response.Status.OK.statusCode)

        val userUnitsAfter = unitRepository.findAllByUser(user1?.id!!).size
        assert(userUnitsBefore - 1 == userUnitsAfter)
    }


    @Test
    @WithDefaultUser
    fun `Deleting unit of other user fails`() {
        val unit = Unit().apply {
            x = 3
            y = 5
            user = user2
            type = UnitType.SWORDSMAN
        }
        unitRepository.save(unit)

        val userUnitsBefore = unitRepository.findAllByUser(user2?.id!!).size

        given()
            .delete("$endpoint/${unit.id}")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)

        val userUnitsAfter = unitRepository.findAllByUser(user2?.id!!).size
        assert(userUnitsBefore == userUnitsAfter)
    }

    @Test
    @WithDefaultUser
    fun `Markets are getting destroyed on conquer`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit2 = Unit().apply {
            x = 3
            y = 3
            user = user2
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit2)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.SWORDSMAN
        }
        unitRepository.save(unit)
        val market = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.MARKET
        }
        buildingRepository.save(market)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        assert(unitRepository.listAll().size == 2)
        assert(buildingRepository.listAll().size == 1)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.SWORDSMAN)
        assert(eventRepository.listAll().size == 3)
        assert(buildingRepository.listAll().size == 0)
    }

    @Test
    @WithDefaultUser
    fun `The max unit limit does not count for units of type WORKER`() {
        val unit1 = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit1)
        val unit2 = Unit().apply {
            x = 3
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
        val unit3 = Unit().apply {
            x = 4
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit3)
        val village = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val castle = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 4)
        userRepository.setBeerTo(user1!!.id!!, 999999);
        val request2 = CreateUnitRequestDto(4, 4, UnitType.SPEARMAN)
        given()
            .body(request2)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 5)
    }

    @Test
    @WithDefaultUser
    fun `The get units endpoint returns the total amount of units and the current limit`() {
        val unit1 = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit1)
        val unit2 = Unit().apply {
            x = 3
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
        val unit3 = Unit().apply {
            x = 4
            y = 2
            user = user1
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit3)
        val castle = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val response = given()
            .`when`()
            .get("$endpoint?x1=0&x2=5&y1=0&y2=5")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UnitsResponseDto::class.java)
        assert(response.units.size == 3)
        assert(response.unitsCount == 1)
        assert(response.unitsLimit == UNITS_PER_CASTLE_LVL_1)
    }

    @Test
    @WithDefaultUser
    fun `A castle level 1 gives 25% chance to not lose`() {
        val mapTile = MapTile().apply {
            x = 4
            y = 4
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        var countAttackWins = 0
        val runs = 50
        for (i in 0 until runs) {
            val unit1 = Unit().apply {
                x = 3
                y = 3
                user = user1
                type = UnitType.SPEARMAN
            }
            unitRepository.save(unit1)
            val unit2 = Unit().apply {
                x = 4
                y = 4
                user = user2
                type = UnitType.HORSEMAN
            }
            unitRepository.save(unit2)
            val castle = Building().apply {
                x = 4
                y = 4
                user = user2
                type = BuildingType.CASTLE
                level = 1
            }
            buildingRepository.save(castle)
            userRepository.setBeerTo(user1!!.id!!, 999)
            val request = MoveUnitRequestDto(4, 4, unit1.id!!)
            given()
                .body(request)
                .`when`()
                .post("$endpoint/move")
                .then()
                .statusCode(Response.Status.OK.statusCode)
            assert(unitRepository.listAll().size == 1)
            if (unitRepository.listAll().first().type == UnitType.SPEARMAN) {
                countAttackWins++
            }
            unitRepository.deleteAll()
            buildingRepository.deleteAll()
        }
        assert(countAttackWins < runs * 0.9)
        assert(countAttackWins > runs * 0.6)
    }

    @Test
    @WithDefaultUser
    fun `A castle level 2 gives 50% chance to not lose`() {
        val mapTile = MapTile().apply {
            x = 4
            y = 4
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        var countAttackWins = 0
        val runs = 50
        for (i in 0 until runs) {
            val unit1 = Unit().apply {
                x = 3
                y = 3
                user = user1
                type = UnitType.SPEARMAN
            }
            unitRepository.save(unit1)
            val unit2 = Unit().apply {
                x = 4
                y = 4
                user = user2
                type = UnitType.HORSEMAN
            }
            unitRepository.save(unit2)
            val castle = Building().apply {
                x = 4
                y = 4
                user = user2
                type = BuildingType.CASTLE
                level = 2
            }
            buildingRepository.save(castle)
            userRepository.setBeerTo(user1!!.id!!, 999)
            val request = MoveUnitRequestDto(4, 4, unit1.id!!)
            given()
                .body(request)
                .`when`()
                .post("$endpoint/move")
                .then()
                .statusCode(Response.Status.OK.statusCode)
            assert(unitRepository.listAll().size == 1)
            if (unitRepository.listAll().first().type == UnitType.SPEARMAN) {
                countAttackWins++
            }
            unitRepository.deleteAll()
            buildingRepository.deleteAll()
        }
        assert(countAttackWins < runs * 0.65)
        assert(countAttackWins > runs * 0.35)
    }

    @Test
    @WithDefaultUser
    fun `Castle defense does not exist on other buildings`() {
        val mapTile = MapTile().apply {
            x = 4
            y = 4
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        var countAttackWins = 0
        val runs = 10
        for (i in 0 until runs) {
            val unit1 = Unit().apply {
                x = 3
                y = 3
                user = user1
                type = UnitType.SPEARMAN
            }
            unitRepository.save(unit1)
            val unit2 = Unit().apply {
                x = 4
                y = 4
                user = user2
                type = UnitType.HORSEMAN
            }
            unitRepository.save(unit2)
            val village = Building().apply {
                x = 4
                y = 4
                user = user2
                type = BuildingType.VILLAGE
                level = 1
            }
            buildingRepository.save(village)
            userRepository.setBeerTo(user1!!.id!!, 999)
            val request = MoveUnitRequestDto(4, 4, unit1.id!!)
            given()
                .body(request)
                .`when`()
                .post("$endpoint/move")
                .then()
                .statusCode(Response.Status.OK.statusCode)
            assert(unitRepository.listAll().size == 1)
            if (unitRepository.listAll().first().type == UnitType.SPEARMAN) {
                countAttackWins++
            }
            unitRepository.deleteAll()
            buildingRepository.deleteAll()
        }
        assert(countAttackWins == runs)
    }

    @Test
    @WithDefaultUser
    fun `One can build dragons and archers`() {
        val castle = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.CASTLE
            level = 2
        }
        buildingRepository.save(castle)
        userRepository.setGoldTo(user1!!.id!!, 999)
        val request = CreateUnitRequestDto(4, 4, UnitType.DRAGON)
        val response = given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.DRAGON)
    }

    @Test
    @WithDefaultUser
    fun `Amount of units is affecting the dragon price too`() {
        val unit1 = Unit().apply {
            x = 3
            y = 3
            user = user1
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit1)
        val castle = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.CASTLE
            level = 2
        }
        buildingRepository.save(castle)
        userRepository.setGoldTo(user1!!.id!!, (DRAGON_BASE_PRICE * UNIT_PRICE_FACTOR).toInt() - 1)
        val request = CreateUnitRequestDto(4, 4, UnitType.DRAGON)
        val response = given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().`as`(ErrorResponseDto::class.java)
        logger.info { "Response: $response" }
        assert(response.message == "serverError.notEnoughGold")
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.SPEARMAN)
        userRepository.setGoldTo(user1!!.id!!, (DRAGON_BASE_PRICE * UNIT_PRICE_FACTOR).toInt())
        val request2 = CreateUnitRequestDto(4, 4, UnitType.DRAGON)
        given()
            .body(request2)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 2)
    }

    @Test
    @WithDefaultUser
    fun `Dragons cannot move on opponent buildings`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.DRAGON
        }
        unitRepository.save(unit)
        val castle = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.CASTLE
            level = 1
        }
        buildingRepository.save(castle)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(unitRepository.listAll().first().x == 2)
    }

    @Test
    @WithDefaultUser
    fun `Dragons can move on own buildings`() {
        val mapTile = MapTile().apply {
            x = 3
            y = 3
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.DRAGON
        }
        unitRepository.save(unit)
        val castle = Building().apply {
            x = 3
            y = 3
            user = user1
            type = BuildingType.CASTLE
            level = 1
        }
        buildingRepository.save(castle)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        userRepository.setGoldTo(user1!!.id!!, 999)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().first().x == 3)
    }

    @Test
    @WithDefaultUser
    fun `I cannot build a dragon on castle level 1`() {
        val castle = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.CASTLE
            level = 1
        }
        buildingRepository.save(castle)
        userRepository.setGoldTo(user1!!.id!!, 999)
        val request = CreateUnitRequestDto(4, 4, UnitType.DRAGON)
        given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(unitRepository.listAll().size == 0)
    }

    @Test
    @WithDefaultUser
    fun `Dragons can be build in castles level 2 for gold instead of beer`() {
        val castle = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.CASTLE
            level = 2
        }
        buildingRepository.save(castle)
        userRepository.setGoldTo(user1!!.id!!, 999)
        val request = CreateUnitRequestDto(4, 4, UnitType.DRAGON)
        val response = given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { "Response: $response" }
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.DRAGON)
        assert(userRepository.findById(user1!!.id!!)?.gold == 999 - DRAGON_BASE_PRICE);
        assert(userRepository.findById(user1!!.id!!)?.beer == START_BEER);
    }

    @Test
    @WithDefaultUser
    fun `Dragons win against all units except archers`() {
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
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit2)
        val unit = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.DRAGON
        }
        unitRepository.save(unit)
        userRepository.setGoldTo(user1!!.id!!, 999)
        val request = MoveUnitRequestDto(3, 3, unit.id!!)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/move")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.DRAGON)
    }
}
