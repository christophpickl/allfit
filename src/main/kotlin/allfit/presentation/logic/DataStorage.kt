package allfit.presentation.logic

import allfit.api.OnefitUtils
import allfit.presentation.PartnerModifications
import allfit.presentation.PresentationConstants
import allfit.presentation.models.DateRange
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.SimplePartner
import allfit.presentation.models.SimpleWorkout
import allfit.service.InMemoryImageStorage
import javafx.scene.image.Image
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

interface DataStorage {
    fun getFutureFullWorkouts(): List<FullWorkout>
    fun getFullPartnerById(partnerId: Int): FullPartner

    // for real impl, here we do database operations
    fun updatePartner(modifications: PartnerModifications)
    fun toFullWorkout(workout: SimpleWorkout): FullWorkout
}

object InMemoryDataStorage : DataStorage {

    private fun readImage(fileName: String) =
        Image(
            InMemoryImageStorage::class.java.getResourceAsStream("/images/$fileName")
                ?: error("Could not find image at path: $fileName"),
            PresentationConstants.tableImageWidth, 0.0, true, true
        )

    private val now = ZonedDateTime.now()
    private val defaultTime = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(2)
    private val defaultDateRange = DateRange(defaultTime, defaultTime.plusHours(1))
    private val pastDateRange = DateRange(defaultTime.minusDays(1), defaultTime.minusDays(1).plusHours(1))

    private val workoutEms = SimpleWorkout(
        id = 1,
        name = "EMS",
        about = "About <b>EMS</b> HTML.",
        specifics = "",
        date = defaultDateRange,
        image = readImage("workouts/ems.jpg"),
        url = OnefitUtils.workoutUrl(11101386, "ems-health-studio-ems-training-ems-training"),
        isReserved = true,
    )
    private val workoutYogaYin = SimpleWorkout(
        id = 2,
        name = "Yin Yoga",
        about = "",
        specifics = "",
        date = DateRange(now.plusDays(1), now.plusDays(1).plusHours(1)),
        image = readImage("workouts/yoga_yin.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val workoutYogaHot = SimpleWorkout(
        id = 3,
        name = "Hot Yoga",
        about = "",
        specifics = "",
        date = DateRange(defaultTime.plusHours(3), defaultTime.plusHours(4).plusMinutes(30)),
        image = readImage("workouts/yoga_hot.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val workoutGym = SimpleWorkout(
        id = 4,
        name = "Open Gym",
        about = "",
        specifics = "",
        date = defaultDateRange,
        image = readImage("workouts/gym.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val workoutJump = SimpleWorkout(
        id = 5,
        name = "Jumping",
        about = "",
        specifics = "",
        date = defaultDateRange,
        image = readImage("workouts/trampoline.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )
    private val pastWorkoutGym = SimpleWorkout(
        id = 6,
        name = "Open Gym",
        about = "",
        specifics = "",
        date = pastDateRange,
        image = readImage("workouts/gym.jpg"),
        url = "https://nu.nl",
        isReserved = false,
    )

    private val futureSimpleWorkouts = listOf(workoutEms, workoutYogaYin, workoutYogaHot, workoutGym, workoutJump)
    private val pastSimpleWorkouts = listOf(pastWorkoutGym)

    private val partnerEms = FullPartner(
        SimplePartner(
            id = 1,
            name = "EMS Studio",
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
        pastWorkouts = listOf(),
        currentWorkouts = listOf(workoutEms),
    )
    private val partnerYoga = FullPartner(
        SimplePartner(
            id = 2,
            name = "Yoga School",
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
        pastWorkouts = listOf(),
        currentWorkouts = listOf(workoutYogaYin, workoutYogaHot)
    )
    private val partnerGym = FullPartner(
        SimplePartner(
            id = 3,
            name = "The Gym",
            checkins = 1,
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
        pastWorkouts = listOf(pastWorkoutGym),
        currentWorkouts = listOf(workoutGym)
    )
    private val partnerFoobar = FullPartner(
        SimplePartner(
            id = 4,
            name = "Foobar",
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
        pastWorkouts = listOf(),
        currentWorkouts = listOf(workoutJump)
    )
    private val allFullPartners = listOf(partnerEms, partnerYoga, partnerGym, partnerFoobar)

    private val allFutureFullWorkouts = futureSimpleWorkouts.map { simpleWorkout ->
        FullWorkout(
            simpleWorkout = simpleWorkout,
            partner = allFullPartners.first { partner ->
                partner.currentWorkouts.map { it.id }.contains(simpleWorkout.id)
            }.simplePartner,
        )
    }
    private val allPastFullWorkouts = pastSimpleWorkouts.map { simpleWorkout ->
        FullWorkout(
            simpleWorkout = simpleWorkout,
            partner = allFullPartners.first { partner ->
                partner.pastWorkouts.map { it.id }.contains(simpleWorkout.id)
            }.simplePartner,
        )
    }
    private val allFullWorkouts = allFutureFullWorkouts + allPastFullWorkouts

    override fun getFutureFullWorkouts() = allFutureFullWorkouts

    override fun getFullPartnerById(partnerId: Int) =
        allFullPartners.firstOrNull { it.id == partnerId }
            ?: error("Could not find partner by ID: $partnerId")

    override fun updatePartner(modifications: PartnerModifications) {
        val storedPartner = getFullPartnerById(modifications.partnerId)
        modifications.update(storedPartner.simplePartner)
    }

    override fun toFullWorkout(workout: SimpleWorkout): FullWorkout =
        allFullWorkouts.firstOrNull { it.id == workout.id } ?: error("Could not find workout by ID: ${workout.id}")
}
