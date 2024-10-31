package eu.dobschal.repository

import eu.dobschal.model.entity.Tutorial
import eu.dobschal.model.enum.TutorialStatus
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import jakarta.ws.rs.BadRequestException

@Transactional
@ApplicationScoped
class TutorialRepository : PanacheRepository<Tutorial> {

    fun findLatestTutorial(userId: Int): Tutorial? {
        return find("user.id = ?1 ORDER BY id DESC", userId).firstResult()
    }

    fun save(tutorial: Tutorial): Tutorial {
        persist(tutorial)
        return tutorial
    }

    fun setStatus(tutorialId: Int, status: TutorialStatus): Tutorial {
        val tutorial =
            find("id = ?1", tutorialId).firstResult() ?: throw BadRequestException("serverError.tutorialNotFound")
        tutorial.status = status
        persist(tutorial)
        return tutorial
    }

    fun findById(id: Int): Tutorial? {
        return find("id", id).firstResult()
    }

}
