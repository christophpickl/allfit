package allfit.api.models

import kotlinx.serialization.Serializable

@Serializable
data class CategoriesJson(
    override val data: List<CategoryJson>
) : SyncableJsonContainer<CategoryJson>

@Serializable
data class CategoryJson(
    override val id: Int,
    override val name: String,
    val slugs: SlugJson,
) : CategoryJsonDefinition

@Serializable
data class SlugJson(
    val nl: String? = null,
    val en: String,
    val es: String? = null,
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