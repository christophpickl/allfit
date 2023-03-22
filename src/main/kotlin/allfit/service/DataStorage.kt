package allfit.service

import allfit.presentation.PartnerModifications
import allfit.presentation.models.FullPartner
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.SimplePartner
import allfit.presentation.models.SimpleWorkout

interface DataStorage {
    fun getAllFullWorkouts(): List<FullWorkout>
    fun getFullPartnerById(partnerId: Int): FullPartner
    fun updatePartner(modifications: PartnerModifications)
}

object InMemoryDataStorage : DataStorage {

    private val workoutEms = SimpleWorkout(id = 1, name = "EMS")
    private val workoutYogaYin = SimpleWorkout(id = 2, name = "Yin Yoga")
    private val workoutYogaHot = SimpleWorkout(id = 3, name = "Hot Yoga")
    private val workoutGym = SimpleWorkout(id = 4, name = "Open Gym")
    private val workoutJump = SimpleWorkout(id = 5, name = "Jumping")
    private val simpleWorkouts = listOf(workoutEms, workoutYogaYin, workoutYogaHot, workoutGym, workoutJump)

    private val partnerEms = FullPartner(
        SimplePartner(id = 1, name = "EMS Studio", visits = 0, rating = 0, isFavourited = false, isWishlisted = false),
        listOf(workoutEms)
    )
    private val partnerYoga = FullPartner(
        SimplePartner(id = 2, name = "Yoga School", visits = 2, rating = 5, isFavourited = true, isWishlisted = false),
        listOf(workoutYogaYin, workoutYogaHot)
    )
    private val partnerGym = FullPartner(
        SimplePartner(id = 3, name = "The Gym", visits = 1, rating = 3, isFavourited = false, isWishlisted = false),
        listOf(workoutGym)
    )
    private val partnerFoobar = FullPartner(
        SimplePartner(id = 4, name = "Foobar", visits = 0, rating = 0, isFavourited = false, isWishlisted = true),
        listOf(workoutJump)
    )
    private val allFullPartners = listOf(partnerEms, partnerYoga, partnerGym, partnerFoobar)

    private val allFullWorkouts = simpleWorkouts.map { simpleWorkout ->
        FullWorkout(
            simpleWorkout = simpleWorkout,
            partner = allFullPartners.first { partner ->
                partner.workouts.map { it.id }.contains(simpleWorkout.id)
            }.simplePartner,
        )
    }

    override fun getAllFullWorkouts() = allFullWorkouts

    override fun getFullPartnerById(partnerId: Int) =
        allFullPartners.firstOrNull { it.id == partnerId }
            ?: error("Could not find partner by ID: $partnerId")

    override fun updatePartner(modifications: PartnerModifications) {
        val storedPartner = getFullPartnerById(modifications.partnerId)
        modifications.update(storedPartner.simplePartner)
    }
}

//class DataStorageImpl(
//    private val categoriesRepo: CategoriesRepo
//): DataStorage {
//    private val lazyCategories: List<Category> by lazy {
//        categoriesRepo.selectAll().filter { !it.isDeleted }.map { it.toCategory() }
//    }
//
//    fun getCategories(): List<Category> = lazyCategories
//}
//
//private fun CategoryEntity.toCategory() = Category(
//    id = id,
//    name = name,
//    isDeleted = isDeleted,
//)
