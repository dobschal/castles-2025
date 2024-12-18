package eu.dobschal.resource

import WithDefaultUser
import eu.dobschal.model.dto.UserRankingDto
import eu.dobschal.model.dto.request.*
import eu.dobschal.model.dto.response.*
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
    @WithDefaultUser
    fun `Getting start village w_o any building returns 404`() {
        assert(buildingRepository.listAll().isEmpty())
        given()
            .`when`()
            .get("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @WithDefaultUser
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
            .`when`()
            .get("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @WithDefaultUser
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
            .`when`()
            .get("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Building::class.java)
        assert(village.x == village1.x)
        assert(village.y == village1.y)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
    fun `Save start village on wrong tile type fails`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.WATER
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = BaseCoordinatesDto(1, 1)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().isEmpty())
    }

    @Test
    @WithDefaultUser
    fun `Save start village works`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = BaseCoordinatesDto(1, 1)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
    fun `Get buildings works`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = BaseCoordinatesDto(1, 1)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .`when`()
            .get("$endpoint?x1=0&x2=2&y1=0&y2=2")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    @WithDefaultUser
    fun `Get buildings returns empty list when no building in range`() {
        val mapTile = MapTile().apply {
            x = 1
            y = 1
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val request = BaseCoordinatesDto(1, 1)
        given()
            .body(request)
            .`when`()
            .post("$endpoint/start-village")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .`when`()
            .get("$endpoint?x1=2&x2=10&y1=2&y2=50")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 0)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 2)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 2)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 2)
        assert(unitRepository.listAll().isEmpty())
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 0)
        assert(unitRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 0)
        assert(unitRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 0)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.isEmpty())
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 3)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 3)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 1)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 3)
        assert(userRepository.findById(user1!!.id!!)!!.beer == START_BEER - BREWERY_BASE_PRICE)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 3)
        assert(unitRepository.listAll().isEmpty())
    }

    @Test
    @WithDefaultUser
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
        userRepository.setBeerTo(user1!!.id!!, 0)
        assert(userRepository.findById(user1!!.id!!)!!.beer == 0)
        val request = CollectBeerRequestDto(brewery.id!!, BREWERY_BEER_STORAGE)
        val response = given()
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == BREWERY_BEER_STORAGE)
    }

    @Test
    @WithDefaultUser
    fun `Collecting beer (calculation is correct) on a brewery works`() {
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
    @WithDefaultUser
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
    @WithDefaultUser
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
    @WithDefaultUser
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
        userRepository.addBeerToUser(user1!!.id!!, VILLAGE_BASE_PRICE * 2 - user1!!.beer!! - 5)
        assert(buildingRepository.listAll().size == 3)
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_BASE_PRICE * 2 - 5)
        val request = CollectBeerRequestDto(brewery.id!!, 5)
        val response = given()
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_BASE_PRICE * 2)
    }

    @Test
    @WithDefaultUser
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
        userRepository.addBeerToUser(user1!!.id!!, VILLAGE_BASE_PRICE * 2 - user1!!.beer!! - 5)
        assert(buildingRepository.listAll().size == 4)
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_BASE_PRICE * 2 - 5)
        val request = CollectBeerRequestDto(brewery.id!!, BREWERY_BEER_STORAGE)
        val response = given()
            .body(request)
            .`when`()
            .post("$endpoint/collect-beer")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().asString()
        logger.info { response }
        assert(userRepository.findById(user1!!.id!!)!!.beer == VILLAGE_BASE_PRICE * 2 + BREWERY_BEER_STORAGE - 5)
    }

    @WithDefaultUser
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
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 2)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 2)
    }

    @Test
    @WithDefaultUser
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
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 2)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 2)
    }

    @Test
    @WithDefaultUser
    fun `Destroying own building works`() {
        val x = 20
        val y = 20
        val farm = Building().apply {
            this.x = x
            this.y = y
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val farmsBeforeDestroy = buildingRepository.countBuildingTypeByUser(user1?.id!!, BuildingType.FARM)

        val request = BaseCoordinatesDto(x, y)
        given()
            .body(request)
            .`when`()
            .delete(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)

        val farmsAfterDestroy = buildingRepository.countBuildingTypeByUser(user1?.id!!, BuildingType.FARM)
        assert(farmsAfterDestroy == farmsBeforeDestroy - 1)
    }

    @Test
    @WithDefaultUser
    fun `Destroying own building fails if last available village`() {
        val x = 14
        val y = 14
        val village = Building().apply {
            this.x = x
            this.y = y
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)

        val request = BaseCoordinatesDto(x, y)
        given()
            .body(request)
            .`when`()
            .delete(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)

    }

    @Test
    @WithDefaultUser
    fun `Destroying building fails if no building`() {
        val request = BaseCoordinatesDto(999, 999)
        given()
            .body(request)
            .`when`()
            .delete(endpoint)
            .then()
            .statusCode(Response.Status.NOT_FOUND.statusCode)
    }

    @Test
    @WithDefaultUser
    fun `Destroying building fails if not own building`() {
        val x = 18
        val y = 18
        val farm = Building().apply {
            this.x = x
            this.y = y
            user = user2
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)

        val request = BaseCoordinatesDto(x, y)
        given()
            .body(request)
            .`when`()
            .delete(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @WithDefaultUser
    fun `A village can only have 2 breweries`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm = Building().apply {
            x = 12
            y = 13
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val brewery = Building().apply {
            x = 14
            y = 15
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
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
        given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 4)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 4)
    }

    @Test
    @WithDefaultUser
    fun `A village can only have 2 breweries (fail)`() {
        val village = Building().apply {
            x = 14
            y = 14
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm = Building().apply {
            x = 12
            y = 13
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm)
        val brewery = Building().apply {
            x = 14
            y = 15
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery)
        val brewery2 = Building().apply {
            x = 14
            y = 17
            user = user1
            type = BuildingType.BREWERY
        }
        buildingRepository.save(brewery2)
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
        given()
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 4)
        val response = given()
            .`when`()
            .get("$endpoint?x1=10&x2=20&y1=10&y2=20")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 4)
    }

    @Test
    @WithDefaultUser
    fun `With two villages I cannot afford a city`() {
        val village1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village1)
        val village2 = Building().apply {
            x = 2
            y = 2
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village2)
        val pricesResponse = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        assert(pricesResponse.buildingPrices[BuildingType.CITY] == CITY_BASE_PRICE)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("$endpoint?x1=0&x2=3&y1=0&y2=3")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 2)
        assert(response.totalBeerStorage < pricesResponse.buildingPrices[BuildingType.CITY]!!)
        assert(response.totalBeerStorage == VILLAGE_BASE_PRICE * 2 * 2)
    }

    @Test
    @WithDefaultUser
    fun `With 3 villages I can afford a city`() {
        val village1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village1)
        val village2 = Building().apply {
            x = 2
            y = 2
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village2)
        val village3 = Building().apply {
            x = 3
            y = 3
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village3)
        val pricesResponse = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        assert(pricesResponse.buildingPrices[BuildingType.CITY] == CITY_BASE_PRICE)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("$endpoint?x1=0&x2=4&y1=0&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 3)
        assert(response.totalBeerStorage >= pricesResponse.buildingPrices[BuildingType.CITY]!!)
        assert(response.totalBeerStorage == VILLAGE_BASE_PRICE * 2 * 2 * 2)
    }

    @Test
    @WithDefaultUser
    fun `Per 2n + 1 villages I can convert a village into a city for the price of a village`() {
        val village1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village1)
        userRepository.setBeerTo(user1!!.id!!, CITY_BASE_PRICE)
        val request = CreateBuildingRequestDto(1, 1, BuildingType.CITY)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post("$endpoint/create-city")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 1) // because the village is deleted
        assert(buildingRepository.countBuildingTypeByUser(user1!!.id!!, BuildingType.CITY) == 1)

    }

    @Test
    @WithDefaultUser
    fun `Cannot upgrade without enough beer`() {
        val village1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village1)
        userRepository.setBeerTo(user1!!.id!!, CITY_BASE_PRICE - 1)
        val request = CreateBuildingRequestDto(1, 1, BuildingType.CITY)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post("$endpoint/create-city")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1) // because the village is deleted
        assert(buildingRepository.countBuildingTypeByUser(user1!!.id!!, BuildingType.VILLAGE) == 1)
    }

    @Test
    @WithDefaultUser
    fun `Cannot upgrade if there is no village`() {
        val village1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(village1)
        userRepository.setBeerTo(user1!!.id!!, CITY_BASE_PRICE - 1)
        val request = CreateBuildingRequestDto(1, 1, BuildingType.CITY)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post("$endpoint/create-city")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        assert(buildingRepository.listAll().size == 1) // because the village is deleted
        assert(buildingRepository.countBuildingTypeByUser(user1!!.id!!, BuildingType.CASTLE) == 1)
    }

    @Test
    @WithDefaultUser
    fun `Second city costs double the price`() {
        val city1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city1)
        val pricesResponse = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        assert(pricesResponse.buildingPrices[BuildingType.CITY] == CITY_BASE_PRICE * 3)
    }

    @Test
    @WithDefaultUser
    fun `Having a city is increasing the price for the next village like I would have create another village`() {
        val village = Building().apply {
            x = 2
            y = 2
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val city1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city1)
        val pricesResponse = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        assert(pricesResponse.buildingPrices[BuildingType.VILLAGE] == VILLAGE_BASE_PRICE * 2 * 2)
    }

    @Test
    @WithDefaultUser
    fun `Having a city is increasing the max beer limit like I would have create another village`() {
        val village1 = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village1)
        val village2 = Building().apply {
            x = 2
            y = 2
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village2)
        val city = Building().apply {
            x = 3
            y = 3
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city)
        val pricesResponse = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("/v1/prices")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(PricesResponseDto::class.java)
        assert(pricesResponse.buildingPrices[BuildingType.CITY] == CITY_BASE_PRICE * 3)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("$endpoint?x1=0&x2=4&y1=0&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(BuildingsResponseDto::class.java)
        assert(response.buildings.size == 3)
        assert(response.totalBeerStorage < pricesResponse.buildingPrices[BuildingType.CITY]!!)
        assert(response.totalBeerStorage == VILLAGE_BASE_PRICE * 2 * 2 * 2)
    }

    @Test
    @WithDefaultUser
    fun `I can create a worker on a city too`() {
        val city = Building().apply {
            x = 1
            y = 1
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city)
        val request = CreateUnitRequestDto(1, 1, UnitType.WORKER)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post("/v1/units")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Unit::class.java)
        assert(response.type == UnitType.WORKER)
        assert(unitRepository.listAll().size == 1)
        assert(unitRepository.listAll().first().type == UnitType.WORKER)
        val response2 = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("/v1/units?x1=0&x2=4&y1=0&y2=4")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UnitsResponseDto::class.java)
        assert(response2.units.size == 1)
        assert(response2.units.first().type == UnitType.WORKER)
    }

    @Test
    @WithDefaultUser
    fun `If I have only one city left, I cannot destroy it`() {
        val x = 14
        val y = 14
        val city = Building().apply {
            this.x = x
            this.y = y
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city)

        val request = BaseCoordinatesDto(x, y)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .delete("/v1/buildings/")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
    }

    @Test
    @WithDefaultUser
    fun `If I have only one city left and I lose it, I am game over`() {
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
        val city = Building().apply {
            x = 3
            y = 4
            user = user2
            type = BuildingType.CITY
        }
        buildingRepository.save(city)
        userRepository.setBeerTo(user2!!.id!!, 10)

        assert(unitRepository.listAll().size == 2)
        assert(buildingRepository.listAll().size == 2)
        assert(userRepository.findById(user2!!.id!!)!!.beer == 10)

        val request = MoveUnitRequestDto(3, 4, unit.id!!)
        val response = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post("/v1/units/move")
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
        assert(buildingRepository.listAll().first().type == BuildingType.CITY)
        assert(eventRepository.listAll().last().type == EventType.GAME_OVER)
    }

    @Test
    @WithDefaultUser
    fun `I can build one farm and two breweries per city or village`() {
        val mapTile = MapTile().apply {
            x = 2
            y = 2
            type = MapTileType.PLAIN
        }
        mapTileRepository.saveMapTiles(setOf(mapTile))
        val village = Building().apply {
            x = 3
            y = 3
            user = user1
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val farm1 = Building().apply {
            x = 4
            y = 4
            user = user1
            type = BuildingType.FARM
        }
        buildingRepository.save(farm1)
        val city = Building().apply {
            x = 5
            y = 5
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city)
        val worker = Unit().apply {
            x = 2
            y = 2
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(worker)
        assert(buildingRepository.listAll().size == 3)
        val request = CreateBuildingRequestDto(2, 2, BuildingType.FARM)
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .body(request)
            .`when`()
            .post(endpoint)
            .then()
            .statusCode(Response.Status.OK.statusCode)
        assert(buildingRepository.listAll().size == 4)
    }

    @Test
    @WithDefaultUser
    fun `Per city the user gets a specific amount of gold storage`() {
        val response = assertOkGetRequest("/v1/buildings", BuildingsResponseDto::class.java)
        assert(response.totalGoldStorage == 0)
        val city = Building().apply {
            x = 5
            y = 5
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city)
        val response2 = assertOkGetRequest("/v1/buildings", BuildingsResponseDto::class.java)
        assert(response2.totalGoldStorage == GOLD_STORAGE_PER_CITY)
        val city2 = Building().apply {
            x = 6
            y = 6
            user = user1
            type = BuildingType.CITY
        }
        buildingRepository.save(city2)
        val response3 = assertOkGetRequest("/v1/buildings", BuildingsResponseDto::class.java)
        assert(response3.totalGoldStorage == GOLD_STORAGE_PER_CITY * 2)
    }

    @Test
    @WithDefaultUser
    fun `A market can be build when owning a city`() {
        val x = 2
        val y = 2
        mapTileRepository.saveMapTiles(setOf(MapTile().apply {
            this.x = x
            this.y = y
            type = MapTileType.PLAIN
        }))
        unitRepository.save(Unit().apply {
            this.x = x
            this.y = y
            user = user1
            type = UnitType.WORKER
        })
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.CITY
        })
        userRepository.setBeerTo(user1!!.id!!, MARKET_BASE_PRICE)
        val request = CreateBuildingRequestDto(x, y, BuildingType.MARKET)
        assertOkPostRequest("/v1/buildings", request, Building::class.java)
    }

    @Test
    @WithDefaultUser
    fun `A market cannot be build when not having a city`() {
        val x = 2
        val y = 2
        mapTileRepository.saveMapTiles(setOf(MapTile().apply {
            this.x = x
            this.y = y
            type = MapTileType.PLAIN
        }))
        unitRepository.save(Unit().apply {
            this.x = x
            this.y = y
            user = user1
            type = UnitType.WORKER
        })
        userRepository.setBeerTo(user1!!.id!!, MARKET_BASE_PRICE)
        val request = CreateBuildingRequestDto(x, y, BuildingType.MARKET)
        val response = assertBadPostRequest("/v1/buildings", request)
        assert(response.message == "serverError.noCity")
    }

    @Test
    @WithDefaultUser
    fun `I can exchange beer for gold in a market`() {
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.MARKET
        })
        val response = assertOkPostRequest(
            "/v1/buildings/sell-beer",
            SellBeerRequest(SELL_BEER_PRICE),
            SuccessResponseDto::class.java
        )
        val user = userRepository.findById(user1!!.id!!)!!
        assert(response.message == "serverSuccess.beerSold")
        assert(user.beer == START_BEER - SELL_BEER_PRICE)
        assert(user.gold == 1)
    }

    @Test
    @WithDefaultUser
    fun `I cannot exchange beer for gold when selecting too less beer`() {
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.MARKET
        })
        val response = assertBadPostRequest(
            "/v1/buildings/sell-beer",
            SellBeerRequest(SELL_BEER_PRICE - 1)
        )
        assert(response.message == "serverError.notEnoughBeer")
    }

    @Test
    @WithDefaultUser
    fun `I cannot exchange beer for gold when not having enough beer in a market`() {
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.MARKET
        })
        val response = assertBadPostRequest(
            "/v1/buildings/sell-beer",
            SellBeerRequest(START_BEER + 1)
        )
        assert(response.message == "serverError.notEnoughBeer")
    }

    @Test
    @WithDefaultUser
    fun `I cannot exchange beer for gold when not having a market`() {
        val response = assertBadPostRequest(
            "/v1/buildings/sell-beer",
            SellBeerRequest(SELL_BEER_PRICE)
        )
        assert(response.message == "serverError.noMarket")
    }

    @Test
    @WithDefaultUser
    fun `With a city I can upgrade a castle to level 2`() {
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.CITY
        })
        val castle = Building().apply {
            this.x = 3
            this.y = 3
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        userRepository.setGoldTo(user1!!.id!!, CASTLE_LEVEL_UP_PRICE)
        val response = assertOkPostRequest(
            "/v1/buildings/level-up",
            LevelUpBuildingRequestDto(castle.id!!),
            SuccessResponseDto::class.java
        )
        assert(response.message == "serverSuccess.buildingLevelUp")
        val updatedCastle = buildingRepository.findById(castle.id!!)
        assert(updatedCastle?.level == 2)
    }

    @Test
    @WithDefaultUser
    fun `The castle upgrade prices are increasing x3 per castle level 2`() {
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.CITY
        })
        val castle = Building().apply {
            this.x = 3
            this.y = 3
            level = 2
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        assertOkGetRequest("/v1/prices", PricesResponseDto::class.java).let {
            assert(it.buildingLevelUpPrices[BuildingType.CASTLE] == CASTLE_LEVEL_UP_PRICE * 3)
        }
        userRepository.setGoldTo(user1!!.id!!, CASTLE_LEVEL_UP_PRICE * 3)
        val response = assertBadPostRequest(
            "/v1/buildings/level-up",
            LevelUpBuildingRequestDto(castle.id!!),
        )
        assert(response.message == "serverError.levelUpNotPossible")
        val updatedCastle = buildingRepository.findById(castle.id!!)
        assert(updatedCastle?.level == 2)
    }

    @Test
    @WithDefaultUser
    fun `Gold is deducted after upgrading castle`() {
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.CITY
        })
        val castle = Building().apply {
            this.x = 3
            this.y = 3
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        userRepository.setGoldTo(user1!!.id!!, CASTLE_LEVEL_UP_PRICE)
        assert(userRepository.findById(user1!!.id!!)!!.gold == CASTLE_LEVEL_UP_PRICE)
        val response = assertOkPostRequest(
            "/v1/buildings/level-up",
            LevelUpBuildingRequestDto(castle.id!!),
            SuccessResponseDto::class.java
        )
        assert(response.message == "serverSuccess.buildingLevelUp")
        val updatedCastle = buildingRepository.findById(castle.id!!)
        assert(updatedCastle?.level == 2)
        assert(userRepository.findById(user1!!.id!!)!!.gold == 0)
    }

    @Test
    @WithDefaultUser
    fun `Without enough gold you cannot upgrade a castle`() {
        buildingRepository.save(Building().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = BuildingType.CITY
        })
        val castle = Building().apply {
            this.x = 3
            this.y = 3
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        userRepository.setGoldTo(user1!!.id!!, CASTLE_LEVEL_UP_PRICE - 1)
        val response = assertBadPostRequest(
            "/v1/buildings/level-up",
            LevelUpBuildingRequestDto(castle.id!!)
        )
        assert(response.message == "serverError.notEnoughGold")
        val updatedCastle = buildingRepository.findById(castle.id!!)
        assert(updatedCastle?.level == 1)
    }

    @Test
    @WithDefaultUser
    fun `You still can build old unit from castles level 2`() {
        val castle = Building().apply {
            this.x = 3
            this.y = 3
            user = user1
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        assertOkPostRequest(
            "/v1/units",
            CreateUnitRequestDto(3, 3, UnitType.SPEARMAN),
            Unit::class.java
        )
        assert(unitRepository.listAll().size == 1)
    }

    @Test
    @WithDefaultUser
    fun `castle level 2 are increasing the unit limit`() {
        unitRepository.save(Unit().apply {
            this.x = 4
            this.y = 4
            user = user1
            type = UnitType.HORSEMAN
        })
        unitRepository.save(Unit().apply {
            this.x = 5
            this.y = 5
            user = user1
            type = UnitType.HORSEMAN
        })
        unitRepository.save(Unit().apply {
            this.x = 6
            this.y = 6
            user = user1
            type = UnitType.HORSEMAN
        })
        unitRepository.save(Unit().apply {
            this.x = 7
            this.y = 7
            user = user1
            type = UnitType.HORSEMAN
        })
        val castle = Building().apply {
            this.x = 3
            this.y = 3
            user = user1
            type = BuildingType.CASTLE
            level = 2
        }
        buildingRepository.save(castle)
        userRepository.setBeerTo(user1!!.id!!, 99999999)
        assertOkPostRequest(
            "/v1/units",
            CreateUnitRequestDto(3, 3, UnitType.SPEARMAN),
            Unit::class.java
        )
        assert(unitRepository.listAll().size == 5)
        assertBadPostRequest(
            "/v1/units",
            CreateUnitRequestDto(3, 3, UnitType.SPEARMAN)
        )
        assert(unitRepository.listAll().size == 5)
    }

    @Test
    @WithDefaultUser
    fun `castle level 2 gives more points in the ranking`() {
        val castle = Building().apply {
            this.x = 3
            this.y = 3
            user = user1
            type = BuildingType.CASTLE
            level = 2
        }
        buildingRepository.save(castle)
        val response = assertOkGetRequest("/v1/users/ranking", Array<UserRankingDto>::class.java)
        assert(response.size == 2)
        assert(response.first { it.username == USER1 }.points == 4)
    }

}
