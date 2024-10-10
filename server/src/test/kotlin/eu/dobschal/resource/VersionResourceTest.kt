package eu.dobschal.resource

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import jakarta.ws.rs.core.Response
import org.junit.jupiter.api.Test

@QuarkusTest
class VersionResourceTest {

    val endpoint = "/v1/version"

    @Test
    fun `Version API is returning a version`() {

        RestAssured.given()
            .get("$endpoint")
            .then()
            .statusCode(Response.Status.OK.statusCode)
    }

}
