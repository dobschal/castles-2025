package eu.dobschal.service

import eu.dobschal.model.dto.UserDto
import eu.dobschal.model.dto.UserRankingDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.User
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.UnitRepository
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
class UserService @Inject constructor(
    private val userRepository: UserRepository,
    private var jwt: JsonWebToken,
    private val buildingRepository: BuildingRepository,
    private val unitRepository: UnitRepository
) {

    private val logger = KotlinLogging.logger {}

    fun listAllRankings(): List<UserRankingDto> {
        val buildings = buildingRepository.listAll()
        val units = unitRepository.listAll()
        return userRepository.listAll().map { user ->
            val oldestBuildingOfUser =
                buildings.filter { it.user?.username == user.username }.minByOrNull { it.createdAt }
            UserRankingDto(
                user.id!!,
                user.username,
                buildings.count { it.user?.username == user.username } * 2 + units.count { it.user?.username == user.username },
                user.avatarId ?: 0,
                oldestBuildingOfUser?.x ?: 0,
                oldestBuildingOfUser?.y ?: 0
            )
        }
    }

    fun getOneRanking(userId: Int): UserRankingDto {
        val user = userRepository.findById(userId)
            ?: throw BadRequestException("User not found")
        val buildings = buildingRepository.findAllByUser(userId)
        val units = unitRepository.findAllByUser(userId)
        return UserRankingDto(
            user.id!!,
            user.username,
            buildings.count() * 2 + units.count(),
            user.avatarId ?: 0,
            buildings.minByOrNull { it.createdAt!! }?.x ?: 0,
            buildings.minByOrNull { it.createdAt!! }?.y ?: 0
        )
    }

    fun registerUser(username: String, password: String): User {

        if (username.isBlank() || password.isBlank()) {
            throw BadRequestException("Username and password must not be empty")
        }
        userRepository.userExists(username).takeIf { it }?.let {
            throw BadRequestException("User already exists")
        }

        return userRepository.createUser(username, hash(password))
    }

    fun loginUser(username: String, password: String): JwtResponseDto {
        if (username.isBlank() || password.isBlank()) {
            throw BadRequestException("Username and password must not be empty")
        }

        val user = userRepository
            .findByUsernameAsDto(username)
            .takeIf { it?.password == hash(password) }
            ?: throw UnauthorizedException("Invalid credentials")

        return JwtResponseDto(
            Jwt.issuer(JWT_ISSUER)
                .upn(user.username)
                .expiresIn(360000000)
                .groups(setOf(USER_ROLE))
                .sign()
        )
    }

    fun getCurrentUserDto(): UserDto {
        val username = jwt.name;
        return userRepository.findByUsernameAsDto(username)
            ?.apply { password = "" }
            ?: throw UnauthorizedException("User not found")
    }

    fun getCurrentUser(): User {
        val username = jwt.name;
        return userRepository.findByUsername(username)
            ?.apply { password = "" }
            ?: throw UnauthorizedException("User not found")
    }

    fun setAvatar(userId: Int, avatarId: Int): User {
        val currentUser = getCurrentUser()
        if (currentUser.id != userId) {
            throw UnauthorizedException("serverError.unauthorized")
        }
        return userRepository.updateAvatar(userId, avatarId)
    }

}
