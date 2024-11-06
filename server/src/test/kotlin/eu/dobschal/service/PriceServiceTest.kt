package eu.dobschal.service

import eu.dobschal.model.entity.Unit
import eu.dobschal.model.entity.User
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
import eu.dobschal.utils.WORKER_BASE_PRICE
import eu.dobschal.utils.hash
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class PriceServiceTest {

    private val logger = KotlinLogging.logger {}

    @Inject
    private lateinit var priceService: PriceService

    @Inject
    private lateinit var userRepository: UserRepository

    @Inject
    private lateinit var mapTileRepository: MapTileRepository

    @Inject
    private lateinit var buildingRepository: BuildingRepository

    @Inject
    private lateinit var unitRepository: UnitRepository

    @Inject
    private lateinit var eventRepository: EventRepository

    var user1: User? = null
    var user2: User? = null

    @AfterEach
    @Transactional
    fun tearDown() {
        mapTileRepository.deleteAll()
        buildingRepository.deleteAll()
        userRepository.deleteAll()
        unitRepository.deleteAll()
        eventRepository.deleteAll()
    }

    @BeforeEach
    fun beforeEach() {
        createUsers()
    }

    @Transactional
    fun createUsers() {
        user1 = userRepository.createUser("user1", hash("password"))
        user2 = userRepository.createUser("user2", hash("password"))
    }

    @Test
    fun `test price calculation`() {
        val price1 = priceService.getPriceForUnitCreation(user1!!.toDto(), UnitType.WORKER)
        assert(price1 == WORKER_BASE_PRICE)
        val unit = Unit().apply {
            x = 1
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit)
        val price2 = priceService.getPriceForUnitCreation(user1!!.toDto(), UnitType.WORKER)
        assert(price2 == WORKER_BASE_PRICE * 2)
        val unit2 = Unit().apply {
            x = 1
            y = 1
            user = user1
            type = UnitType.WORKER
        }
        unitRepository.save(unit2)
        val price3 = priceService.getPriceForUnitCreation(user1!!.toDto(), UnitType.WORKER)
        assert(price3 == WORKER_BASE_PRICE * 2 * 2)

    }
}
