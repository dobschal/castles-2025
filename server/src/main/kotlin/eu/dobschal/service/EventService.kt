package eu.dobschal.service

import eu.dobschal.repository.EventRepository
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.LocalDateTime

@ApplicationScoped
class EventService @Inject constructor(private val eventRepository: EventRepository) {

    // Instead of comparing by createdAt, we can compare by id because
    // the id is auto-incremented and the events are saved in order --> faster
    var thresholdId: Int = 0

    @Scheduled(every = "5m")
    fun setEventThresholdId() {
        eventRepository.findFirstEventAfter(LocalDateTime.now().minusHours(12))?.let {
            thresholdId = it.id!!
        }
    }

    fun getEventsBetween(x1: Int, x2: Int, y1: Int, y2: Int, lastEventId: Int?): List<Any> =
        eventRepository.findEventsBetween(x1, x2, y1, y2, lastEventId ?: thresholdId)

}
