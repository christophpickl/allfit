package allfit.api.models

import kotlinx.serialization.Serializable

//interface CommonCategoryJson {
//    val id: Int
//    val name: String
//}

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