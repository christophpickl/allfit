package allfit.api.models

import kotlinx.serialization.Serializable

interface PagedJson<ENTITY> {
    val data: List<ENTITY>
    val meta: MetaJson
}

@Serializable
data class MetaJson(
    val pagination: MetaPaginationJson,
) {
    companion object {
        val empty = MetaJson(MetaPaginationJson.empty)
    }
}

@Serializable
data class MetaPaginationJson(
    val total: Int, // total item count
    val count: Int, // current page item count
    val per_page: Int, // page size
    val current_page: Int,
    val total_pages: Int,
) {
    companion object {
        val empty = MetaPaginationJson(
            total = 0,
            count = 0,
            per_page = 10,
            current_page = 1,
            total_pages = 1,
        )
    }
}

@Serializable
data class SlugJson(
    val nl: String? = null,
    val en: String,
    val es: String? = null,
)
