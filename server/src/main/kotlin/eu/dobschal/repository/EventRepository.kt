package eu.dobschal.repository

import eu.dobschal.model.dto.EventDto
import eu.dobschal.model.entity.Event
import eu.dobschal.model.enum.EventType
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

    fun findEventByXAndYAndType(x: Int, y: Int, type: EventType): Event? {
        return find("x = ?1 and y = ?2 and type = ?3 ORDER BY id DESC", x, y, type).firstResult()
    }
}

