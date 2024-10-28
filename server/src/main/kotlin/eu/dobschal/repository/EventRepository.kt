package eu.dobschal.repository

import eu.dobschal.model.dto.EventDto
import eu.dobschal.model.entity.Event
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional


@Transactional
@ApplicationScoped
class EventRepository @Inject constructor(private val entityManager: EntityManager) : PanacheRepository<Event> {

    fun save(event: Event) {
        persist(event)
    }

    fun findEventsBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<EventDto> {
        val result = find("#Event.findEventsBetween", x1, x2, y1, y2)
            .project(EventDto::class.java)
            .list()
        return result
    }
}

