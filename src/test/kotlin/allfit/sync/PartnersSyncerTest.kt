package allfit.sync

import allfit.api.models.HeaderImageJson
import allfit.api.models.PartnerJson
import allfit.api.models.PartnersJson
import allfit.api.models.partnerCategoryJson
import allfit.api.models.partnerJson
import allfit.api.models.partnerSubCategoryJson
import allfit.persistence.InMemoryCategoriesRepo
import allfit.persistence.InMemoryLocationsRepo
import allfit.persistence.InMemoryPartnersRepo
import allfit.persistence.PartnerEntity
import allfit.service.InMemoryImageStorage
import allfit.service.PartnerAndImageUrl
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class PartnersSyncerTest : StringSpec() {

    private val imageUrl = "imageUrl"
    private val partner = Arb.partnerJson().next()

    private lateinit var syncer: PartnersSyncer
    private lateinit var imageStorage: InMemoryImageStorage
    private lateinit var categoriesRepo: InMemoryCategoriesRepo
    private lateinit var partnersRepo: InMemoryPartnersRepo
    private lateinit var locationsRepo: InMemoryLocationsRepo

    override suspend fun beforeEach(testCase: TestCase) {
        imageStorage = InMemoryImageStorage()
        categoriesRepo = InMemoryCategoriesRepo()
        partnersRepo = InMemoryPartnersRepo()
        locationsRepo = InMemoryLocationsRepo()
        syncer = PartnersSyncer(partnersRepo, imageStorage)
    }

    init {
        "When sync partner Then insert entity" {
            sync(partner)

            partnersRepo.selectAll().shouldBeSingleton().first() shouldBe PartnerEntity(
                id = partner.id,
                categoryIds = mutableListOf<Int>().also {
                    it += partner.category.id
                    it += partner.categories.map { it.id }
                },
                name = partner.name,
                slug = partner.slug,
                description = partner.description,
                imageUrl = partner.header_image.orig,
                facilities = partner.facilities.joinToString(","),
                note = "",
                isDeleted = false,
                isFavorited = false,
                isStarred = false,
                isHidden = false,

                )
        }
        "When sync partner with duplicate categories Then insert single only" {
            sync(
                partner.copy(
                    category = Arb.partnerCategoryJson().next().copy(id = 1), categories = listOf(
                        Arb.partnerSubCategoryJson().next().copy(id = 1),
                        Arb.partnerSubCategoryJson().next().copy(id = 1),
                    )
                )
            )

            partnersRepo.selectAll().shouldBeSingleton().first()
                .categoryIds.shouldBeSingleton().first() shouldBe 1
        }
        "When sync partner Then image is saved" {
            sync(partner.copy(header_image = HeaderImageJson(imageUrl)))

            imageStorage.savedPartnerImages.shouldBeSingleton().first() shouldBe PartnerAndImageUrl(
                partner.id,
                imageUrl
            )
        }
    }

    private suspend fun sync(vararg partners: PartnerJson) =
        syncer.sync(PartnersJson(partners.toList()))
}
