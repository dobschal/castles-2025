package eu.dobschal.service

import eu.dobschal.model.entity.User
import eu.dobschal.repository.UserRepository
import eu.dobschal.utils.JWT_ISSUER
import eu.dobschal.utils.USER_ROLE
import eu.dobschal.utils.hash
import io.quarkus.security.UnauthorizedException
import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import mu.KotlinLogging
import org.eclipse.microprofile.jwt.JsonWebToken


@ApplicationScoped
class UserService @Inject constructor(private val userRepository: UserRepository, private var jwt: JsonWebToken) {

    private val logger = KotlinLogging.logger {}

    fun registerUser(username: String, password: String): User {
        if (username.isBlank() || password.isBlank()) {
            throw BadRequestException("Username and password must not be empty")
        }
        userRepository.userExists(username).takeIf { it }?.let {
            throw BadRequestException("User already exists")
        }

        return userRepository.createUser(username, hash(password))
    }

    fun loginUser(username: String, password: String): String {
        if (username.isBlank() || password.isBlank()) {
            throw BadRequestException("Username and password must not be empty")
        }

        val user = userRepository
            .findByUsername(username)
            .takeIf { it?.password == hash(password) }
            ?: throw UnauthorizedException("Invalid credentials")

        return Jwt.issuer(JWT_ISSUER)
            .upn(user.username)
            .groups(setOf(USER_ROLE))
            .sign()
    }

    fun getCurrentUser(): User {
        val username = jwt.name;
        return userRepository.findByUsername(username)
            ?.apply { password = "" }
            ?: throw UnauthorizedException("User not found")
    }

}
