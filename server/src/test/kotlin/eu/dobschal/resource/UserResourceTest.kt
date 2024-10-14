package eu.dobschal.resource

import eu.dobschal.model.dto.UserCredentialsDto
import eu.dobschal.model.entity.User
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
import org.junit.jupiter.api.Test

@QuarkusTest
class UserResourceTest {

    private val logger = KotlinLogging.logger {}

    @Inject
    private lateinit var userRepository: UserRepository

    val endpoint = "/v1/users"

    @AfterEach
    @Transactional
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `Login for none existing user fails`() {
        given()
            .body(UserCredentialsDto("non-existing-user", "password"))
            .header("Content-Type", MediaType.APPLICATION_JSON)
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
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .post("$endpoint/login")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `Login with correct password returns valid JWT`() {
        userRepository.createUser("existing-user", hash("password"))
        val jwt = given()
            .body(UserCredentialsDto("existing-user", "password"))
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .post("$endpoint/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract()
            .asString()
        logger.info { "JWT from working login: $jwt" }
    }

    @Test
    fun `Registration with existing username fails`() {
        userRepository.createUser("existing-user", hash("password"))
        given()
            .body(UserCredentialsDto("existing-user", "password"))
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .post("$endpoint/register")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        val amountOfUsers = userRepository.findAll().count()
        assert(amountOfUsers.toInt() == 1)
    }

    @Test
    fun `Registration without username or password fails`() {
        given()
            .body("{}")
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .post("$endpoint/register")
            .then()
            .statusCode(Response.Status.BAD_REQUEST.statusCode)
        val amountOfUsers = userRepository.findAll().count()
        assert(amountOfUsers.toInt() == 0)
    }

    @Test
    fun `Registration with new username works`() {
        userRepository.createUser("existing-user", hash("password"))
        given()
            .body(UserCredentialsDto("existing-user-2", "password-2"))
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .post("$endpoint/register")
            .then()
            .statusCode(Response.Status.OK.statusCode)
        val amountOfUsers = userRepository.findAll().count()
        assert(amountOfUsers.toInt() == 2)
    }

    @Test
    fun `Secured route is not accessible without JWT`() {
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .get("$endpoint/current")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `Secured route is not accessible with wrong JWT`() {
        given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer fnsdlgvndflkgdvn")
            .`when`()
            .get("$endpoint/current")
            .then()
            .statusCode(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun `Secured route is accessible with valid JWT`() {
        userRepository.createUser("existing-user", hash("password"))
        val jwt = given()
            .body(UserCredentialsDto("existing-user", "password"))
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .`when`()
            .post("$endpoint/login")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract()
            .asString()
        val user = given()
            .header("Content-Type", MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt")
            .`when`()
            .get("$endpoint/current")
            .then()
            .statusCode(Response.Status.OK.statusCode)
            .extract().`as`(User::class.java)
        assert(user.username == "existing-user")
    }

}
