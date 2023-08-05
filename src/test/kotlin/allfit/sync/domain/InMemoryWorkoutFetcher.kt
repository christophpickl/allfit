package allfit.sync.domain

class InMemoryWorkoutFetcher : WorkoutFetcher {

    val urlToFetches = mutableMapOf<WorkoutUrl, WorkoutFetch>()

    override suspend fun fetch(url: WorkoutUrl) =
        urlToFetches[url] ?: error("Fetch result not found for: $url")
}
