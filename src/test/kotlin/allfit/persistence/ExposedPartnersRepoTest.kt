package allfit.persistence

import allfit.persistence.domain.CategoriesTable
import allfit.persistence.domain.CategoryEntity
import allfit.persistence.domain.ExposedCategoriesRepo
import allfit.persistence.domain.ExposedPartnersRepo
import allfit.persistence.domain.PartnerCategoryEntity
import allfit.persistence.domain.PartnerEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedPartnersRepoTest : DescribeSpec() {

    private val repo = ExposedPartnersRepo
    private val partner = Arb.partnerEntity().next()
    private val category = Arb.categoryEntity().next()
    private val category1 = Arb.categoryEntity().next()
    private val category2 = Arb.categoryEntity().next()

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.selectAll()
            }
        }
        describe("When insert") {
            it("Given partner and his categories Then partner and category references are persisted") {
                ExposedCategoriesRepo.insertAll(listOf(category1, category2))
                val partnerWithCategory = partner.copy(
                    primaryCategoryId = category1.id,
                    secondaryCategoryIds = listOf(category2.id)
                )

                repo.insertAll(listOf(partnerWithCategory))

                repo.selectAll().shouldBeSingleton().first() shouldBe partnerWithCategory
                repo.selectAllPartnerCategories().shouldContainExactlyInAnyOrder(
                    listOf(
                        PartnerCategoryEntity(partnerId = partner.id, categoryId = category1.id, isPrimary = true),
                        PartnerCategoryEntity(partnerId = partner.id, categoryId = category2.id, isPrimary = false),
                    )
                )
            }
            it("Given no category Then fail with foreign key violation") {
                shouldThrow<ExposedSQLException> {
                    ExposedPartnersRepo.insertAll(
                        listOf(
                            partner.copy(
                                primaryCategoryId = category.id,
                                secondaryCategoryIds = emptyList()
                            )
                        )
                    )
                }.message shouldContain category.id.toString()
            }
        }
        describe("When delete") {
            it("Given nothing Then fail") {
                shouldThrow<IllegalArgumentException> {
                    repo.deleteAll(emptyList())
                }
            }
            it("Given non-deleted partner Then mark as deleted") {
                val partnerNotDeleted = insertPartner { it.copy(isDeleted = false) }

                repo.deleteAll(listOf(partnerNotDeleted.id))

                repo.selectAll().shouldBeSingleton().first().isDeleted shouldBe true
            }
            it("Given two non-deleted partners Then mark only one as deleted") {
                val partnerToBeDeleted = insertPartner { it.copy(id = 1, isDeleted = false) }
                insertPartner { it.copy(id = 2, isDeleted = false) }

                repo.deleteAll(listOf(partnerToBeDeleted.id))

                repo.selectAll().filter { it.isDeleted }.shouldBeSingleton().first().also {
                    it.id shouldBe partnerToBeDeleted.id
                    it.isDeleted shouldBe true
                }
            }
        }
    }

    private fun insertPartner(mutateDomainObject: (PartnerEntity) -> PartnerEntity): PartnerEntity {
        ExposedCategoriesRepo.insertIfNotExists(listOf(category))
        val partnerWithCategory = partner.copy(
            primaryCategoryId = category.id,
            secondaryCategoryIds = emptyList(),
        ).let(mutateDomainObject)
        repo.insertAll(listOf(partnerWithCategory))
        return partnerWithCategory
    }
}

private fun ExposedCategoriesRepo.insertIfNotExists(categories: List<CategoryEntity>) {
    transaction {
        val categoryIds = categories.map { it.id }
        val existingCategoryIds = CategoriesTable.select { CategoriesTable.id inList categoryIds }
            .map { it[CategoriesTable.id].value }
        val toInsertCategoryIds = categoryIds.subtract(existingCategoryIds.toSet())
        val toInsertCategories = categories.filter { toInsertCategoryIds.contains(it.id) }
        insertAll(toInsertCategories)
    }
}
