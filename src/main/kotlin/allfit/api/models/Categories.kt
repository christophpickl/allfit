package allfit.api.models

import kotlinx.serialization.Serializable

interface CategoryJsonDefinition : SyncableJson {
    val name: String
    val slugs: SlugJson?
}

@Serializable
data class CategoriesJsonRoot(
    override val data: List<CategoryJson>
) : SyncableJsonContainer<CategoryJson>

@Serializable
data class CategoryJson(
    override val id: Int,
    override val name: String,
    override val slugs: SlugJson,
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


and what we get from partners sub model:
      "category": {
        "id": 1,
        "name": "Gym",
        "slugs": {
          "nl": "gym",
          "en": "gym",
          "es": "gym"
        }
      },
      "categories": [
        {
          "id": 46,
          "name": "Spinning"
        }
      ],
*/