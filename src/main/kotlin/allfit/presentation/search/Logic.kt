package allfit.presentation.search

interface SubSearchRequest<T> {
    val predicate: (T) -> Boolean
}

data class SearchRequest<T>(
    val subSearchRequests: Set<SubSearchRequest<T>>,
    val alwaysIncludeSearchRequest: SubSearchRequest<T>,
) {
    companion object {
        fun <T> empty() = SearchRequest<T>(emptySet(), alwaysTrue())
        fun <T> alwaysTrue() = object : SubSearchRequest<T> {
            override val predicate: (T) -> Boolean = { true }
        }
    }

    val predicate: (T) -> Boolean = { entity ->
        (subSearchRequests + alwaysIncludeSearchRequest).all {
            it.predicate(entity)
        }
    }
}
