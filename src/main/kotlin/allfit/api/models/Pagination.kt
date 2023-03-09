package allfit.api.models

import kotlinx.serialization.Serializable

interface PagedJson<ENTITY> {
    val data: List<ENTITY>
    val meta: MetaJson
}

@Serializable
data class MetaJson(
    val pagination: MetaPaginationJson,
)

@Serializable
data class MetaPaginationJson(
    val current_page: Int,
    val total_pages: Int,
)
