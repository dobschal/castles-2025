package eu.dobschal.repository

import eu.dobschal.model.dto.EventDto
import eu.dobschal.model.entity.Event
import eu.dobschal.model.enum.EventType
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import java.time.LocalDateTime


@Transactional
@ApplicationScoped
class EventRepository @Inject constructor() : PanacheRepository<Event> {

    // Instead of comparing by createdAt, we can compare by id because
    // the id is auto-incremented and the events are saved in order --> faster
    var thresholdId: Int = 0

    @Scheduled(every = "5m")
    fun setEventThresholdId() {
        find("createdAt > ?1 ORDER BY id DESC", LocalDateTime.now().minusHours(12)).firstResult()?.let {
            thresholdId = it.id!!
        }
    }

    fun save(event: Event) {
        persist(event)
    }

    fun findEventsBetween(x1: Int, x2: Int, y1: Int, y2: Int): List<EventDto> {
        val result = find(
            "#Event.findEventsBetween",
            x1,
            x2,
            y1,
            y2,
            thresholdId
        )
            .project(EventDto::class.java)
            .list()
        return result
    }

    fun findEventByXAndYAndType(x: Int, y: Int, type: EventType): Event? {
        return find("x = ?1 and y = ?2 and type = ?3 ORDER BY id DESC", x, y, type).firstResult()
    }

    fun countEventsByUnitIdAndTypeLastHour(unitId: Int, type: EventType): Int {
        return count(
            "unit.id = ?1 AND type = ?2 AND createdAt > ?3",
            unitId,
            type,
            LocalDateTime.now().minusHours(1)
        ).toInt()
    }

    fun findEventByTypeAndUser(userId: Int, eventType: EventType): Event? {
        return find("user1.id = ?1 and type = ?2", userId, eventType).firstResult()
    }
}

