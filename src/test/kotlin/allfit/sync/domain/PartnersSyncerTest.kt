package allfit.sync.domain

import allfit.api.models.HeaderImageJson
import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJsonRoot
import allfit.api.models.partnerCategoryJson
import allfit.api.models.partnerJson
import allfit.api.models.partnerSubCategoryJson
import allfit.persistence.domain.InMemoryCategoriesRepo
import allfit.persistence.domain.InMemoryLocationsRepo
import allfit.persistence.domain.InMemoryPartnersRepo
import allfit.persistence.domain.InMemorySinglesRepo
import allfit.persistence.domain.PartnerEntity
import allfit.service.InMemoryImageStorage
import allfit.service.PartnerAndImageUrl
import allfit.sync.core.SyncListenerManager
import allfit.sync.core.SyncListenerManagerImpl
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class PartnersSyncerTest : StringSpec() {

    private val imageUrl = "imageUrl"
    private val partnerJson = Arb.partnerJson().next()

    private lateinit var syncer: PartnersSyncer
    private lateinit var imageStorage: InMemoryImageStorage
    private lateinit var categoriesRepo: InMemoryCategoriesRepo
    private lateinit var partnersRepo: InMemoryPartnersRepo
    private lateinit var locationsRepo: InMemoryLocationsRepo
    private lateinit var listeners: SyncListenerManager
    private val singlesRepo = InMemorySinglesRepo()

    override suspend fun beforeEach(testCase: TestCase) {
        imageStorage = InMemoryImageStorage()
        categoriesRepo = InMemoryCategoriesRepo()
        partnersRepo = InMemoryPartnersRepo()
        locationsRepo = InMemoryLocationsRepo()
        listeners = SyncListenerManagerImpl()
        syncer = PartnersSyncerImpl(partnersRepo, imageStorage, listeners, singlesRepo)
    }

    init {
        "When sync partner Then insert entity" {
            sync(partnerJson)

            partnersRepo.selectAll().shouldBeSingleton().first() shouldBe PartnerEntity(
                id = partnerJson.id,
                primaryCategoryId = partnerJson.category.id,
                secondaryCategoryIds = partnerJson.categories.map { it.id },
                name = partnerJson.name,
                slug = partnerJson.slug,
                description = partnerJson.description,
                imageUrl = partnerJson.header_image?.orig,
                facilities = partnerJson.facilities.joinToString(","),
                note = "",
                rating = 0,
                isDeleted = false,
                isFavorited = false,
                isWishlisted = false,
                isHidden = false,
                locationShortCode = singlesRepo.selectLocation().shortCode,
                hasDropins = partnerJson.settlement_options.drop_in_enabled,
                hasWorkouts = partnerJson.settlement_options.reservable_workouts,
            )
        }
        "When sync partner with duplicate secondary categories Then insert only one" {
            sync(
                partnerJson.copy(
                    category = Arb.partnerCategoryJson().next().copy(id = 42),
                    categories = listOf(
                        Arb.partnerSubCategoryJson().next().copy(id = 2),
                        Arb.partnerSubCategoryJson().next().copy(id = 2),
                    )
                )
            )

            partnersRepo.selectAll().shouldBeSingleton().first().also {
                it.secondaryCategoryIds shouldContainExactly listOf(2)
            }
        }
        "When sync partner with duplicate primary and secondary category Then insert only primary" {
            sync(
                partnerJson.copy(
                    category = Arb.partnerCategoryJson().next().copy(id = 1),
                    categories = listOf(
                        Arb.partnerSubCategoryJson().next().copy(id = 1),
                        Arb.partnerSubCategoryJson().next().copy(id = 1),
                    )
                )
            )

            partnersRepo.selectAll().shouldBeSingleton().first().also {
                it.primaryCategoryId shouldBe 1
                it.secondaryCategoryIds.shouldBeEmpty()
            }
        }
        "When sync partner Then image is saved" {
            sync(partnerJson.copy(header_image = HeaderImageJson(imageUrl)))

            imageStorage.savedPartnerImages.shouldBeSingleton().first() shouldBe PartnerAndImageUrl(
                partnerJson.id,
                imageUrl
            )
        }
    }

    private suspend fun sync(vararg partners: PartnerJson) =
        syncer.sync(PartnersJsonRoot(partners.toList()))
}