package eu.dobschal.repository

import eu.dobschal.model.dto.UserDto
import eu.dobschal.model.entity.User
import eu.dobschal.utils.START_BEER
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@Transactional
@ApplicationScoped
class UserRepository : PanacheRepository<User> {

    fun findById(id: Int): User? {
        return find("id", id).firstResult()
    }

    fun findByUsernameAsDto(username: String): UserDto? {
        return find("#User.findByUsername", username)
            .project(UserDto::class.java)
            .firstResult()
    }

    fun findByUsername(username: String): User? {
        return find("username", username).firstResult()
    }

    fun userExists(username: String): Boolean {
        return find("username", username).count() > 0
    }

    fun createUser(username: String, password: String): User {
        val user = User().apply {
            this.username = username
            this.password = password
            this.beer = START_BEER
        }
        persist(user)
        return user
    }

    fun deductBeerFromUser(userId: Int, amount: Int) {
        val user = findById(userId) ?: return
        user.beer = user.beer?.minus(amount)
        persist(user)
    }

}
