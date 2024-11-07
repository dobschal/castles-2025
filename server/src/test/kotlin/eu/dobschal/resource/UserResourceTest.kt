package eu.dobschal.resource

import WithDefaultUser
import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.dto.UserRankingDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.utils.hash
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@QuarkusTest
class UserResourceTest : BaseResourceTest() {

    val endpoint = "/v1/users"

    @Test
    fun `Login for none existing user fails`() {
        given()
            .body(UserCredentialsDto("non-existing-user", "password"))
            .`when`()
            .post("$endpoint/login")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `Login with wrong password fails`() {
        userRepository.createUser("existing-user", hash("password"))
        given()
            .body(UserCredentialsDto("existing-user", "wrong-password"))
            .`when`()
            .post("$endpoint/login")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `Login with correct password returns valid JWT`() {
        userRepository.createUser("existing-user", hash("password"))
        given()
            .body(UserCredentialsDto("existing-user", "password"))
            .`when`()
            .post("$endpoint/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
    }

    @Test
    fun `Registration with existing username fails`() {
        userRepository.createUser("existing-user", hash("password"))
        given()
            .body(UserCredentialsDto("existing-user", "password"))
            .`when`()
            .post("$endpoint/register")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        val amountOfUsers = userRepository.findAll().count()
        assert(amountOfUsers.toInt() == 3)
    }

    @Test
    fun `Registration without username or password fails`() {
        given()
            .body("{}")
            .`when`()
            .post("$endpoint/register")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        val amountOfUsers = userRepository.findAll().count()
        assert(amountOfUsers.toInt() == 2)
    }

    @Test
    fun `Registration with new username works`() {
        userRepository.createUser("existing-user", hash("password"))
        given()
            .body(UserCredentialsDto("existing-user-2", "password-2"))
            .`when`()
            .post("$endpoint/register")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        val amountOfUsers = userRepository.findAll().count()
        assert(amountOfUsers.toInt() == 4)
    }

    @Test
    fun `Secured route is not accessible without JWT`() {
        given()
            .`when`()
            .get("$endpoint/current")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `Secured route is not accessible with wrong JWT`() {
        given()
            .header("Authorization", "Bearer fnsdlgvndflkgdvn")
            .`when`()
            .get("$endpoint/current")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `Secured route is accessible with valid JWT`() {
        userRepository.createUser("existing-user", hash("password"))
        val response = given()
            .body(UserCredentialsDto("existing-user", "password"))
            .`when`()
            .post("$endpoint/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(JwtResponseDto::class.java)
        val user = given()
            .header("Authorization", "Bearer ${response.jwt}")
            .`when`()
            .get("$endpoint/current")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(User::class.java)
        assert(user.username == "existing-user")
    }

    @Test
    @WithDefaultUser
    fun `GET users rankings returns list of all users with points`() {
        val castle = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val village = Building().apply {
            x = 3
            y = 2
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val unit2 = Unit().apply {
            x = 3
            y = 2
            user = user2
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit2)

        val unit1 = Unit().apply {
            x = 3
            y = 30
            user = user1
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit1)

        val response = given()
            .`when`()
            .get("$endpoint/ranking")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<UserRankingDto>::class.java)

        assert(response.size == 2)
        val user1Response = response.find { it.username == "user1" }
        val user2Response = response.find { it.username == "user2" }
        assert(user1Response?.points == 1)
        assert(user2Response?.points == 5)
    }

    @Test
    @WithDefaultUser
    fun `Ranking contains oldest village`() {
        val castle = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val village = Building().apply {
            x = 3
            y = 2
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val village2 = Building().apply {
            x = 20
            y = 20
            user = user2
            type = BuildingType.VILLAGE
            createdAt = LocalDateTime.now().minusDays(1)
        }
        buildingRepository.save(village2)
        val unit2 = Unit().apply {
            x = 3
            y = 2
            user = user2
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit2)

        val unit1 = Unit().apply {
            x = 3
            y = 30
            user = user1
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit1)

        val response = given()
            .`when`()
            .get("$endpoint/ranking")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(Array<UserRankingDto>::class.java)

        assert(response.size == 2)
        val user2Response = response.find { it.username == "user2" }
        assert(user2Response?.x == 20)
        assert(user2Response?.y == 20)
    }

    @Test
    @WithDefaultUser
    fun `GET user ranking returns user with points`() {
        val castle = Building().apply {
            x = 3
            y = 3
            user = user2
            type = BuildingType.CASTLE
        }
        buildingRepository.save(castle)
        val village = Building().apply {
            x = 3
            y = 2
            user = user2
            type = BuildingType.VILLAGE
        }
        buildingRepository.save(village)
        val unit2 = Unit().apply {
            x = 3
            y = 2
            user = user2
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit2)
        val unit1 = Unit().apply {
            x = 3
            y = 30
            user = user2
            type = UnitType.SPEARMAN
        }
        unitRepository.save(unit1)

        val response = given()
            .`when`()
            .get("$endpoint/${user2?.id}/ranking")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UserRankingDto::class.java)
        assert(response?.points == 6)
    }

    @Test
    @WithDefaultUser
    fun `Can change avatar id`() {
        given()
            .`when`()
            .put("$endpoint/${user1?.id}/avatar/2")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        val response = given()
            .`when`()
            .get("$endpoint/${user1?.id}/ranking")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UserRankingDto::class.java)
        assert(response?.avatarImageId == 2)
    }

    @Test
    fun `Cannot change avatar id of other players`() {
        val jwt1 = getJwt(USER1)
        val jwt2 = getJwt(USER2)
        given()
            .header("Authorization", "Bearer $jwt2")
            .`when`()
            .put("$endpoint/${user1?.id}/avatar/2")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
        val response = given()
            .header("Authorization", "Bearer $jwt1")
            .`when`()
            .get("$endpoint/${user1?.id}/ranking")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(UserRankingDto::class.java)
        assert(response?.avatarImageId == 0)
    }

}
