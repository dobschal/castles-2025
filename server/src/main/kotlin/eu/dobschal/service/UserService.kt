package eu.dobschal.service

import eu.dobschal.model.dto.BuildingDto
import eu.dobschal.model.dto.UserDto
import eu.dobschal.model.dto.UserRankingDto
import eu.dobschal.model.dto.response.JwtResponseDto
import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.repository.BuildingRepository
import eu.dobschal.repository.UnitRepository
import eu.dobschal.repository.UserRepository
import eu.dobschal.utils.JWT_ISSUER
import eu.dobschal.utils.MAP_MAX
import eu.dobschal.utils.USER_ROLE
import eu.dobschal.utils.hash
import io.quarkus.security.UnauthorizedException
import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.core.SecurityContext
import mu.KotlinLogging


@ApplicationScoped
class UserService @Inject constructor(
    private val userRepository: UserRepository,
    private val buildingRepository: BuildingRepository,
    private val unitRepository: UnitRepository,
    private val securityContext: SecurityContext
) {

    private val logger = KotlinLogging.logger {}

    fun calculatePoints(user: User, buildings: List<BuildingDto>, units: List<Unit>): Int {
        var pointsForBuildings = 0;
        buildings.filter { it.user?.id == user.id }.forEach {
            if (it.type == BuildingType.CITY) {
                pointsForBuildings += 4
            } else if (it.type != BuildingType.CITY) {
                pointsForBuildings += it.level?.times(2) ?: 0
            }
        }
        val pointsForUnits = units.count { it.user?.username == user.username }
        return pointsForBuildings + pointsForUnits
    }

    fun listAllRankings(): List<UserRankingDto> {
        val buildings = buildingRepository.findBuildingsBetween(-MAP_MAX, MAP_MAX, -MAP_MAX, MAP_MAX)
        val units = unitRepository.listAll()
        return userRepository.listAll().map { user ->
            val oldestBuildingOfUser =
                buildings.filter { it.user?.username == user.username }.minByOrNull { it.createdAt!! }
            UserRankingDto(
                user.id!!,
                user.username,
                calculatePoints(user, buildings, units),
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
            calculatePoints(user, buildings, units),
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
        val username = getUserName()
        return userRepository.findByUsernameAsDto(username)
            ?.apply { password = "" }
            ?: throw UnauthorizedException("User not found")
    }

    fun getCurrentUser(): User {
        val username = getUserName()
        return userRepository.findByUsername(username)
            ?.apply { password = "" }
            ?: throw UnauthorizedException("User not found")
    }

    fun getUserName(): String {
        val principal = securityContext.userPrincipal
        return principal.name
    }

    fun setAvatar(userId: Int, avatarId: Int): User {
        val currentUser = getCurrentUser()
        if (currentUser.id != userId) {
            throw UnauthorizedException("serverError.unauthorized")
        }
        return userRepository.updateAvatar(userId, avatarId)
    }

}
