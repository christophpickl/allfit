package allfit.api.models

import kotlinx.serialization.Serializable

@Serializable
data class SlugJson(
    val nl: String? = null,
    val en: String,
    val es: String? = null,
)
