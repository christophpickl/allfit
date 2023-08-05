package allfit.service

class InMemoryWorkoutInserter : WorkoutInserter {

    val workoutsInserted = mutableListOf<List<InsertWorkout>>()

    override suspend fun insert(workouts: List<InsertWorkout>, listener: WorkoutInsertListener) {
        workoutsInserted += workouts
    }
}