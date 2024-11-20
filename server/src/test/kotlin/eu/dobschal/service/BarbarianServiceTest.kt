package eu.dobschal.service

import eu.dobschal.model.entity.Building
import eu.dobschal.model.entity.MapTile
import eu.dobschal.model.enum.BuildingType
import eu.dobschal.model.enum.MapTileType
import eu.dobschal.model.enum.UnitType
import eu.dobschal.repository.*
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
class BarbarianServiceTest {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var mapTileRepository: MapTileRepository

    @Inject
    lateinit var buildingRepository: BuildingRepository

    @Inject
    lateinit var unitRepository: UnitRepository

    @Inject
    lateinit var eventRepository: EventRepository

    @Inject
    lateinit var barbarianService: BarbarianService

    @BeforeEach
    fun setup() {
        createRandomMapTiles()
        val user1 = userRepository.createUser("user1", "password")
        val user2 = userRepository.createUser("user2", "password")
        buildingRepository.persist(Building().apply {
            x = 13
            y = 13
            type = BuildingType.VILLAGE
            user = user1
        })
        buildingRepository.persist(Building().apply {
            x = 66
            y = 66
            type = BuildingType.VILLAGE
            user = user2
        })
    }

    @AfterEach
    fun tearDown() {
        mapTileRepository.deleteAll()
        buildingRepository.deleteAll()
        userRepository.deleteAll()
        unitRepository.deleteAll()
        eventRepository.deleteAll()
    }

    private fun createRandomMapTiles(): Set<MapTile> {
        val mapTiles = mutableSetOf<MapTile>()
        for (i in 0..100) {
            for (j in 0..100) {
                mapTiles.add(MapTile().apply {
                    x = i
                    y = j
                    type = MapTileType.pickOneRandomly()
                })
            }
        }
        mapTileRepository.saveMapTiles(mapTiles)
        return mapTiles
    }

    @Test
    fun `controlBarbarians creates barbarian user and barbarian units if it does not exist`() {
        barbarianService.controlBarbarians()
        assert(userRepository.userExists("barbarian"))
        val amount = unitRepository.countUnitsByUser(userRepository.findByUsername("barbarian")!!.id!!)
        assert(amount == 6) // (2 users + barbarian user) * 1.75 = 5.25 --> ceil --> 6
        val unit1 = unitRepository.listAll().first()
        barbarianService.controlBarbarians()
        assert(unitRepository.countUnitsByUser(userRepository.findByUsername("barbarian")!!.id!!) == 6)
        val unit2 = unitRepository.listAll().first()
        assert(unit1.id == unit2.id)
        assert(unit1.x != unit2.x || unit1.y != unit2.y)
        assert(unitRepository.listAll().none { it.type == UnitType.DRAGON || it.type == UnitType.ARCHER })
    }

}
