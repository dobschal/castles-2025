package eu.dobschal.repository

import eu.dobschal.model.dto.UserDto
import eu.dobschal.model.entity.User
import eu.dobschal.utils.START_BEER
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException

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
            this.gold = 0
            this.avatarId = 0
        }
        persist(user)
        return user
    }

    fun deductBeerFromUser(userId: Int, amount: Int) {
        val user = findById(userId) ?: return
        user.beer = user.beer?.minus(amount)
        persist(user)
    }

    fun addBeerToUser(userId: Int, amountOfBeer: Int) {
        val user = findById(userId) ?: return
        user.beer = user.beer?.plus(amountOfBeer)
        persist(user)
    }

    fun countUsers(): Int {
        return count().toInt()
    }

    fun updateAvatar(userId: Int, avatarId: Int): User {
        val user = findById(userId) ?: throw NotFoundException("serverError.userNotFound")
        user.avatarId = avatarId
        persist(user)
        return user
    }

    fun setBeerTo(userId: Int, startBeer: Int) {
        val user = findById(userId) ?: return
        user.beer = startBeer
        persist(user)
    }

    fun addGoldToUser(userId: Int, amountOfGold: Int) {
        val user = findById(userId) ?: return
        user.gold = user.gold?.plus(amountOfGold)
        persist(user)
    }

    fun deductGoldFromUser(userId: Int, gold: Int) {
        val user = findById(userId) ?: return
        user.gold = user.gold?.minus(gold)
        persist(user)
    }

    fun setGoldTo(userId: Int, gold: Int) {
        val user = findById(userId) ?: return
        user.gold = gold
        persist(user)
    }

}
