package allfit.sync.domain

class InMemoryWorkoutFetcher : WorkoutMetadataFetcher {

    val urlToFetches = mutableMapOf<WorkoutUrl, WorkoutFetchMetadata>()

    override suspend fun fetch(url: WorkoutUrl) =
        urlToFetches[url] ?: error("Fetch result not found for: $url")
}
