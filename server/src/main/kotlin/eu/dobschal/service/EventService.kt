package eu.dobschal.service

import eu.dobschal.model.entity.Event
import eu.dobschal.repository.EventRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class EventService @Inject constructor(private val eventRepository: EventRepository) {

    fun getEventsBetween(x1: Int, x2: Int, y1: Int, y2: Int, lastEventId: Int): List<Event> {
        return eventRepository.getEventsBetween(x1, x2, y1, y2, lastEventId)
    }

}
