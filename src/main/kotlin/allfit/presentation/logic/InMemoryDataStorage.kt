package allfit.presentation.logic

import allfit.api.OnefitUtils
import allfit.presentation.PartnerModifications
import allfit.presentation.PresentationConstants
import allfit.presentation.models.Checkin
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.SimplePartner
import allfit.presentation.models.SimpleWorkout
import allfit.service.Clock
import allfit.service.InMemoryImageStorage
import java.time.temporal.ChronoUnit
import javafx.scene.image.Image

class InMemoryDataStorage(clock: Clock) : DataStorage {

    private fun readImage(fileName: String) =
        Image(
            InMemoryImageStorage::class.java.getResourceAsStream("/images/$fileName")
                ?: error("Could not find image at path: $fileName"),
            PresentationConstants.tableImageWidth, 0.0, true, true
        )

    private val defaultDateTime = clock.now().truncatedTo(ChronoUnit.HOURS).plusHours(2)
    private val defaultDateRange = DateRange(defaultDateTime, defaultDateTime.plusHours(1))
    private val pastDateRange = DateRange(defaultDateTime.minusDays(1), defaultDateTime.minusDays(1).plusHours(1))

    private val partnerEmsId = 1
    private val partnerYogaId = 2
    private val partnerGymId = 3
    private val partnerFoobarId = 4

    private val workoutEms = SimpleWorkout(
        id = 1,
        partnerId = partnerEmsId,
        name = "EMS",
        about = "About <b>EMS</b> HTML.",
        specifics = "About specifics in HTML.",
        date = defaultDateRange,
        address = "Main Street 1",
        image = readImage("workouts/ems.jpg"),
        url = OnefitUtils.workoutUrl(11101386, "ems-health-studio-ems-training-ems-training"),
        isReserved = true,
    )
    private val workoutYogaYin = SimpleWorkout(
        id = 2,
        partnerId = partnerYogaId,
        name = "Yin Yoga",
        about = "About yoga.",
        specifics = "Specifics.",
        address = "",
        date = DateRange(defaultDateTime.plusDays(1), defaultDateTime.plusDays(1).plusHours(1)),
        image = readImage("workouts/yoga_yin.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val workoutYogaHot = SimpleWorkout(
        id = 3,
        partnerId = partnerYogaId,
        name = "Hot Yoga",
        about = "",
        specifics = "",
        address = "",
        date = DateRange(defaultDateTime.plusHours(3), defaultDateTime.plusHours(4).plusMinutes(30)),
        image = readImage("workouts/yoga_hot.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val workoutGym = SimpleWorkout(
        id = 4,
        partnerId = partnerGymId,
        name = "Open Gym",
        about = "",
        specifics = "",
        address = "",
        date = defaultDateRange,
        image = readImage("workouts/gym.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val workoutJump = SimpleWorkout(
        id = 5,
        partnerId = partnerFoobarId,
        name = "Jumping",
        about = "",
        specifics = "",
        address = "",
        date = defaultDateRange,
        image = readImage("workouts/trampoline.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val visitedWorkoutGym = SimpleWorkout(
        id = 6,
        partnerId = partnerGymId,
        name = "Open Gym",
        about = "",
        specifics = "",
        address = "",
        date = pastDateRange,
        image = readImage("workouts/gym.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )

    private val upcomingSimpleWorkouts = listOf(workoutEms, workoutYogaYin, workoutYogaHot, workoutGym, workoutJump)
    private val pastSimpleWorkouts = listOf(visitedWorkoutGym)

    private val partnerEms = FullPartner(
        SimplePartner(
            id = partnerEmsId,
            name = "EMS Studio",
            categories = listOf("EMS"),
            checkins = 0,
            rating = 0,
            isFavorited = false,
            isWishlisted = false,
            isHidden = false,
            image = readImage("partners/ems.jpg"),
            url = OnefitUtils.partnerUrl(16456, "ems-health-studio-ems-training-amsterdam"),
            note = "This URL actually works.",
            description = "Super intense nice <b>workout</b> with HTML.",
            facilities = "",
        ),
        pastCheckins = emptyList(),
        upcomingWorkouts = listOf(workoutEms),
    )
    private val partnerYoga = FullPartner(
        SimplePartner(
            id = partnerYogaId,
            name = "Yoga School",
            categories = listOf("Yoga", "Breathwork"),
            checkins = 0,
            rating = 5,
            isFavorited = true,
            isWishlisted = false,
            isHidden = false,
            image = readImage("partners/yoga.jpg"),
            url = "https://nu.nl",
            note = "my custom note",
            description = "Esoteric stuff.",
            facilities = "Mats",
        ),
        pastCheckins = emptyList(),
        upcomingWorkouts = listOf(workoutYogaYin, workoutYogaHot)
    )
    private val partnerGym = FullPartner(
        SimplePartner(
            id = partnerGymId,
            name = "The Gym & Co",
            categories = listOf(
                "Gym", "Yoga", "Abs", "Pilates", "Boxing", "Martial Arts", "Karate", "Massage",
                "Nuri Nuri", "Whatever", "Nothing", "Everything", "Some", "Other", "Many", "Categories"
            ),
            checkins = 2,
            rating = 3,
            isFavorited = false,
            isWishlisted = false,
            isHidden = false,
            image = readImage("partners/gym.jpg"),
            url = "https://nu.nl",
            note = "",
            description = "Train your body.",
            facilities = "Shower,Locker",
        ),
        listOf(
            Checkin.DropinCheckin(defaultDateTime.minusDays(7)),
            Checkin.WorkoutCheckin(visitedWorkoutGym)
        ),
        upcomingWorkouts = listOf(workoutGym)
    )
    private val partnerFoobar = FullPartner(
        SimplePartner(
            id = partnerFoobarId,
            name = "Foobar",
            categories = emptyList(),
            checkins = 0,
            rating = 0,
            isFavorited = false,
            isWishlisted = true,
            isHidden = false,
            image = readImage("partners/foobar.jpg"),
            url = "https://nu.nl",
            note = "This is weird.",
            description = "Haha.",
            facilities = "",
        ),
        pastCheckins = emptyList(),
        upcomingWorkouts = listOf(workoutJump)
    )
    private val allFullPartners = listOf(partnerEms, partnerYoga, partnerGym, partnerFoobar)

    private val allUpcomingFullWorkouts = upcomingSimpleWorkouts.map { simpleWorkout ->
        FullWorkout(
            simpleWorkout = simpleWorkout,
            partner = allFullPartners.first { partner ->
                partner.upcomingWorkouts.map { it.id }.contains(simpleWorkout.id)
            }.simplePartner,
        )
    }
    private val allVisitedFullWorkouts = pastSimpleWorkouts.map { simpleWorkout ->
        FullWorkout(
            simpleWorkout = simpleWorkout,
            partner = allFullPartners.first { partner ->
                partner.pastCheckins.filterIsInstance<Checkin.WorkoutCheckin>().map { it.workout.id }
                    .contains(simpleWorkout.id)
            }.simplePartner,
        )
    }
    private val allFullWorkouts = allUpcomingFullWorkouts + allVisitedFullWorkouts

    override fun getUpcomingWorkouts() = allUpcomingFullWorkouts

    override fun getPartnerById(partnerId: Int) =
        allFullPartners.firstOrNull { it.id == partnerId }
            ?: error("Could not find partner by ID: $partnerId")

    override fun updatePartner(modifications: PartnerModifications) {
        val storedPartner = getPartnerById(modifications.partnerId)
        modifications.update(storedPartner.simplePartner)
    }

    override fun hidePartner(partnerId: Int) {
        getPartnerById(partnerId).isHidden = true
    }

    override fun unhidePartner(partnerId: Int) {
        getPartnerById(partnerId).isHidden = false
    }

    override fun getWorkoutById(workoutId: Int): FullWorkout =
        allFullWorkouts.firstOrNull { it.id == workoutId } ?: error("Could not find workout by ID: $workoutId")

    override fun getCategories(): List<String> =
        allFullPartners.map { it.categories }.flatten().distinct().sorted()

    override fun getPartners(): List<FullPartner> =
        allFullPartners
}