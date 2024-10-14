package eu.dobschal.resource

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test

@QuarkusTest
class UserResourceTest {

    val endpoint = "/v1/users"

    @Test
    fun `Version API is returning a version`() {

        RestAssured.given()
            .with(Reg)
            .post("$endpoint/register")
            .then()
            .statusCode(Response.Status.OK.statusCode)
    }

}
