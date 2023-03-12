package allfit.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
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

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.selectAll()
            }
        }
        describe("When insert") {
            it("Given category Then partner is persisted") {
                ExposedCategoriesRepo.insertAll(listOf(category))
                val partnerWithCategory = partner.copy(categoryIds = listOf(category.id))

                repo.insertAll(listOf(partnerWithCategory))

                repo.selectAll().shouldBeSingleton().first() shouldBe partnerWithCategory
            }
            it("Given no category Then fail with foreign key violation") {
                shouldThrow<ExposedSQLException> {
                    ExposedPartnersRepo.insertAll(listOf(partner.copy(categoryIds = listOf(category.id))))
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
        val partnerWithCategory = partner.copy(categoryIds = listOf(category.id)).let(mutateDomainObject)
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
