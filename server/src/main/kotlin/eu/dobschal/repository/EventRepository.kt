package eu.dobschal.repository

import eu.dobschal.model.entity.Event
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional


@Transactional
@ApplicationScoped
class EventRepository : PanacheRepository<Event> {

    fun save(event: Event) {
        persist(event)
    }

    fun getEventsBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<Event> {
        return find(
            "x >= ?1 and x < ?2 and y >= ?3 and y < ?4 ORDER BY id DESC",
            x1,
            x2,
            y1,
            y2
        ).list()
    }
}

