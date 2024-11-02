package eu.dobschal.resource

import eu.dobschal.model.dto.request.CreateBuildingRequestDto
import eu.dobschal.model.dto.request.BaseCoordinatesDto
import eu.dobschal.model.dto.response.BuildingsResponseDto
import eu.dobschal.model.dto.response.CollectBeerRequestDto
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
        val request = BaseCoordinatesDto(2, 3)
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
        val request = BaseCoordinatesDto(1, 1)
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
        val request = BaseCoordinatesDto(1, 1)
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
        val request = BaseCoordinatesDto(1, 1)
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
        val request = BaseCoordinatesDto(1, 1)
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
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    fun `Get buildings returns empty list when no building in range`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = BaseCoordinatesDto(1, 1)
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
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 0)
    }

    @Test
    fun `Create building should work`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.FARM)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 2)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 2)
    }

    @Test
    fun `Create building should kill the worker`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.FARM)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 2)
        assert(unitRepository.listAll().isEmpty())
    }

    @Test
    fun `Create building not on PLAIN should fail`() {
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.WATER
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.FARM)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 0)
        assert(unitRepository.listAll().size == 1)
    }

    @Test
    fun `Create building without enough beer should fail`() {
        userRepository.deductBeerFromUser(user1!!.id!!, 451)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.FARM)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 0)
        assert(unitRepository.listAll().size == 1)
    }

    @Test
    fun `Create building on other building should fail`() {
        val village1 = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village1)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.FARM)
        assert(buildingRepository.listAll().size == 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    fun `Create brewery without farm next to it should fail`() {
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.BREWERY)
        assert(buildingRepository.listAll().size == 0)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 0)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 0)
    }

    @Test
    fun `Create brewery with opponents farm next to it should fail`() {
        val farm1 = Building().apply {
            x = 12
            y = 12
            user = user2
            type = BuildingType.FARM
        }
        buildingRepository.save(farm1)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.BREWERY)
        assert(buildingRepository.listAll().size == 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    fun `Create brewery next to farm next should work`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm1 = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm1)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.BREWERY)
        assert(buildingRepository.listAll().size == 2)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 3)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 3)
    }

    @Test
    fun `Create building without worker on field should fail`() {
        val farm1 = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm1)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.SWORDSMAN
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.BREWERY)
        assert(buildingRepository.listAll().size == 1)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    fun `Create building should deduct beer from user`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm1 = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm1)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.BREWERY)
        assert(buildingRepository.listAll().size == 2)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 3)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER - BREWERY_BASE_PRICE)
    }

    @Test
    fun `Worker dies after creating a building`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm1 = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm1)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.BREWERY)
        assert(buildingRepository.listAll().size == 2)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 3)
        assert(unitRepository.listAll().isEmpty())
    }

    @Test
    fun `Collecting beer on a brewery works`() {
        val village = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val brewery = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
        val farm = Building().apply {
            x = 11
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        assert(buildingRepository.listAll().size == 3)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
        val request = CollectBeerRequestDto(brewery.id!!, BREWERY_BEER_STORAGE)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER + BREWERY_BEER_STORAGE)
    }

    @Test
    fun `Collecting beer (calculation is corerct) on a brewery works`() {
        val village = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val brewery = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
        val farm = Building().apply {
            x = 11
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        eventRepository.save(Event().apply {
            user1 = user1
            type = EventType.BEER_COLLECTED
            building = brewery
            x = 12
            y = 12
            createdAt = LocalDateTime.now().minusMinutes(30)
        })
        assert(buildingRepository.listAll().size == 3)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
        val request = CollectBeerRequestDto(brewery.id!!, BREWERY_BEER_PRODUCTION_PER_HOUR / 2)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER + BREWERY_BEER_PRODUCTION_PER_HOUR / 2)
    }

    @Test
    fun `Collecting beer on a farm should fail`() {
        val village = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        assert(buildingRepository.listAll().size == 2)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
        val request = CollectBeerRequestDto(farm.id!!, BREWERY_BEER_STORAGE)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
    }

    @Test
    fun `Collecting beer on an opponents farm should fail`() {
        val village = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val brewery = Building().apply {
            x = 12
            y = 12
            user = user2
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
        assert(buildingRepository.listAll().size == 2)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
        val request = CollectBeerRequestDto(brewery.id!!, BREWERY_BEER_STORAGE)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
    }

    @Test
    fun `Collecting beer with village storage full should be limited`() {
        val village = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val brewery = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
        val farm = Building().apply {
            x = 11
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        userRepository.addBeerToUser(user1!!.id!!, VILLAGE_LEVEL_1_BEER_STORAGE - user1!!.beer!! - 5)
        assert(buildingRepository.listAll().size == 3)
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_LEVEL_1_BEER_STORAGE - 5)
        val request = CollectBeerRequestDto(brewery.id!!, 5)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_LEVEL_1_BEER_STORAGE)
    }

    @Test
    fun `User with more villages can store more beer`() {
        val village = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val village2 = Building().apply {
            x = 15
            y = 15
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village2)
        val brewery = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
        val farm = Building().apply {
            x = 11
            y = 12
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        userRepository.addBeerToUser(user1!!.id!!, VILLAGE_LEVEL_1_BEER_STORAGE - user1!!.beer!! - 5)
        assert(buildingRepository.listAll().size == 4)
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_LEVEL_1_BEER_STORAGE - 5)
        val request = CollectBeerRequestDto(brewery.id!!, BREWERY_BEER_STORAGE)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_LEVEL_1_BEER_STORAGE + BREWERY_BEER_STORAGE - 5)
    }

    @Test
    fun `A brewery without farm does not produce beer`() {
        val village = Building().apply {
            x = 13
            y = 13
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val brewery = Building().apply {
            x = 12
            y = 12
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
        assert(buildingRepository.listAll().size == 2)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
        val request = CollectBeerRequestDto(brewery.id!!, BREWERY_BEER_STORAGE)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER)
    }

    @Test
    fun `A village can only have 1 farm`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm = Building().apply {
            x = 14
            y = 15
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.FARM)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 2)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 2)
    }

    @Test
    fun `A village can only have 1 castle`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val castle = Building().apply {
            x = 14
            y = 15
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val unit = Unit().apply {
            x = 13
            y = 13
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val mapTile = MapTile().apply {
            x = 13
            y = 13
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = CreateBuildingRequestDto(13, 13, BuildingType.CASTLE)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 2)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 2)
    }

}
