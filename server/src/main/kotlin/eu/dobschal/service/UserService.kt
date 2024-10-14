package eu.dobschal.service

import eu.dobschal.model.entity.User
import eu.dobschal.repository.UserRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class UserService @Inject constructor(private val userRepository: UserRepository) {

    fun registerUser(username: String, password: String): User {
        if (username.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Username and password must not be empty")
        }
        userRepository.userExists(username).takeIf { it }?.let {
            throw IllegalArgumentException("User already exists")
        }
        return userRepository.createUser(username, password)
    }

    fun loginUser(username: String, password: String): User {
        if (username.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("Username and password must not be empty")
        }
        val user = userRepository.findByUsername(username).takeIf { it?.password == password }
            ?: throw IllegalArgumentException("Invalid credentials")
        
    }

}
