package allfit.api.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoriesJson(
    val data: List<CategoryJson>
)

@Serializable
data class CategoryJson(
    val id: Int,
    val name: String,
    val slugs: SlugJson,
)

@Serializable
data class SlugJson(
    val nl: String?,
    val en: String,
    val es: String?,
)

/*
{
  "data": [
    {
      "id": 89,
      "name": "Aerial",
      "slugs": {
        "nl": "aerial",
        "en": "aerial",
        "es": "aereo"
      }
    },
    ...
  ]
}
*/