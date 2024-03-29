package allfit.presentation.logic

import allfit.api.OnefitUtils
import allfit.domain.Location
import allfit.presentation.PresentationConstants
import allfit.presentation.models.Checkin
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.HIDDEN_IMAGE
import allfit.presentation.models.NOT_HIDDEN_IMAGE
import allfit.presentation.models.SimplePartner
import allfit.presentation.models.SimpleWorkout
import allfit.presentation.models.Trilean
import allfit.presentation.partners.PartnerModifications
import allfit.service.Clock
import allfit.service.InMemoryImageStorage
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger
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

    private val workoutIdCounter = AtomicInteger(1)
    private val workoutEms = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerEmsId,
        name = "EMS",
        about = "About <b>EMS</b> HTML.",
        specifics = "About specifics in HTML.",
        teacher = "",
        date = defaultDateRange,
        address = "Spuistraat 42, 1012 VE Amsterdam",
        url = OnefitUtils.workoutUrl(11101386, "ems-health-studio-ems-training-ems-training"),
        isReserved = true,
        wasVisited = false,
    )
    private val workoutYogaYin = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerYogaId,
        name = "Yin Yoga was sometimes yin yoga but sometimes it is more cold than hot you know",
        about = "About yoga.",
        specifics = "Specifics.",
        teacher = "Anna Nym",
        address = "",
        date = DateRange(defaultDateTime.plusDays(1), defaultDateTime.plusDays(1).plusHours(1)),
        url = "https://nu.nl",
        isReserved = false,
        wasVisited = false,
    )
    private val workoutYogaHot = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerYogaId,
        name = "Hot Yoga",
        about = "",
        specifics = "",
        teacher = "",
        address = "",
        date = DateRange(defaultDateTime.plusHours(3), defaultDateTime.plusHours(4).plusMinutes(30)),
        url = "https://nu.nl",
        isReserved = false,
        wasVisited = false,
    )
    private val workoutGym = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerGymId,
        name = "Open Gym",
        about = "",
        specifics = "",
        teacher = "",
        address = "",
        date = defaultDateRange,
        url = "https://nu.nl",
        isReserved = false,
        wasVisited = false,
    )
    private val workoutJump = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerFoobarId,
        name = "Jumping",
        about = "",
        specifics = "",
        teacher = "",
        address = "",
        date = defaultDateRange,
        url = "https://nu.nl",
        isReserved = false,
        wasVisited = false,
    )
    private val pastWorkoutGym = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerGymId,
        name = "Open Gym",
        about = "",
        specifics = "",
        teacher = "Raul Teacher",
        address = "",
        date = pastDateRange,
        url = "https://nu.nl",
        isReserved = false,
        wasVisited = true,
    )
    private val workoutYogaCold = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerYogaId,
        name = "Cold Yoga",
        about = "",
        specifics = "",
        teacher = "",
        address = "",
        date = DateRange(defaultDateTime.plusDays(3), defaultDateTime.plusDays(3).plusMinutes(45)),
        url = "https://nu.nl",
        isReserved = false,
        wasVisited = false,
    )
    private val pastYogaWorkout = SimpleWorkout(
        id = workoutIdCounter.getAndIncrement(),
        partnerId = partnerYogaId,
        name = "Visited",
        about = "",
        specifics = "",
        teacher = "",
        address = "",
        date = DateRange(defaultDateTime.minusMonths(1), defaultDateTime.minusMonths(1)),
        url = "https://nu.nl",
        isReserved = false,
        wasVisited = true,
    )

    private val yogaArtificialWorkouts = 1.rangeTo(30).map {
        val startDate = defaultDateTime.plusDays(3).plusHours(it.toLong())
        SimpleWorkout(
            id = workoutIdCounter.getAndIncrement(),
            partnerId = partnerYogaId,
            name = "Workout #$it",
            about = "",
            specifics = "",
            teacher = "",
            address = "",
            date = DateRange(startDate, startDate.plusMinutes(45)),
            url = "https://nu.nl",
            isReserved = false,
            wasVisited = false,
        )
    }

    private val upcomingSimpleWorkouts = listOf(workoutEms, workoutYogaYin, workoutYogaHot, workoutYogaCold, workoutGym, workoutJump) + yogaArtificialWorkouts

    private val pastSimpleWorkouts = listOf(pastWorkoutGym, pastYogaWorkout)

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
            hiddenImage = NOT_HIDDEN_IMAGE,
            image = readImage("partners/ems.jpg"),
            url = OnefitUtils.partnerUrl(16456, "ems-health-studio-ems-training-amsterdam"),
            note = "This URL actually works.",
            officialWebsite = "https://www.ems.com",
            description = "Super intense nice <b>workout</b> with HTML.",
            facilities = "",
            location = Location.Amsterdam,
            hasDropins = Trilean.No,
            hasWorkouts = Trilean.Yes,
        ),
        pastCheckins = emptyList(),
        upcomingWorkouts = listOf(workoutEms),
    )
    private val partnerYoga = FullPartner(
        SimplePartner(
            id = partnerYogaId,
            name = "Yoga School of the north with lots of other offers because they can't do anything else",
            categories = listOf("Yoga", "Breathwork"),
            checkins = 1,
            rating = 5,
            isFavorited = true,
            isWishlisted = false,
            isHidden = false,
            hiddenImage = NOT_HIDDEN_IMAGE,
            image = readImage("partners/yoga.jpg"),
            url = "https://nu.nl",
            officialWebsite = null,
            note = "my custom note",
            description = "Esoteric stuff.",
            facilities = "Mats",
            location = Location.Amsterdam,
            hasDropins = Trilean.Unknown,
            hasWorkouts = Trilean.Unknown,
        ),
        pastCheckins = listOf(Checkin.WorkoutCheckin(pastYogaWorkout)),
        upcomingWorkouts = listOf(workoutYogaYin, workoutYogaHot, workoutYogaCold) + yogaArtificialWorkouts
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
            hiddenImage = NOT_HIDDEN_IMAGE,
            image = readImage("partners/gym.jpg"),
            url = "https://nu.nl",
            officialWebsite = null,
            note = "",
            description = "Train your body.",
            facilities = "Shower,Locker",
            location = Location.Amsterdam,
            hasDropins = Trilean.Yes,
            hasWorkouts = Trilean.No,
        ),
        pastCheckins = listOf(
            Checkin.DropinCheckin(defaultDateTime.minusDays(7)),
            Checkin.WorkoutCheckin(pastWorkoutGym)
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
            hiddenImage = NOT_HIDDEN_IMAGE,
            image = readImage("partners/foobar.jpg"),
            url = "https://nu.nl",
            officialWebsite = null,
            note = "This is weird.",
            description = "Haha.",
            facilities = "",
            location = Location.Amsterdam,
            hasDropins = Trilean.Yes,
            hasWorkouts = Trilean.Yes,
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

    override fun getWorkouts() = allUpcomingFullWorkouts + allVisitedFullWorkouts

    override fun getPartnerById(partnerId: Int) =
        allFullPartners.firstOrNull { it.id == partnerId }
            ?: error("Could not find partner by ID: $partnerId")

    override fun updatePartner(modifications: PartnerModifications) {
        val storedPartner = getPartnerById(modifications.partnerId)
        modifications.update(storedPartner.simplePartner)
    }

    override fun hidePartner(partnerId: Int) {
        val partner = getPartnerById(partnerId)
        partner.isHidden = true
        partner.hiddenImage = HIDDEN_IMAGE
    }

    override fun unhidePartner(partnerId: Int) {
        val partner = getPartnerById(partnerId)
        partner.isHidden = false
        partner.hiddenImage = NOT_HIDDEN_IMAGE
    }

    override fun getFullWorkoutById(workoutId: Int): FullWorkout =
        allFullWorkouts.firstOrNull { it.id == workoutId } ?: error("Could not find workout by ID: $workoutId")

    override fun getCategories(): List<String> =
        allFullPartners.map { it.categories }.flatten().distinct().sorted()

    override fun getPartners(): List<FullPartner> =
        allFullPartners
}