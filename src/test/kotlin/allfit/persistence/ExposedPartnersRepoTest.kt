package allfit.persistence

import allfit.domain.Category
import allfit.domain.Partner
import allfit.domain.category
import allfit.domain.partner
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedPartnersRepoTest : DescribeSpec() {

    private val repo = ExposedPartnersRepo
    private val partner = Arb.partner().next()
    private val category = Arb.category().next()

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.select()
            }
        }
        describe("When insert") {
            it("Given category Then partner is persisted") {
                ExposedCategoriesRepo.insert(listOf(category))
                val partnerWithCategory = partner.copy(categories = listOf(category))

                repo.insert(listOf(partnerWithCategory))

                repo.select().shouldBeSingleton().first() shouldBe partnerWithCategory
            }
            it("Given no category Then fail with foreign key violation") {
                shouldThrow<CategoryNotFoundException> {
                    ExposedPartnersRepo.insert(listOf(partner.copy(categories = listOf(category))))
                }
            }
        }
        describe("When delete") {
            it("Given nothing Then fail") {
                shouldThrow<IllegalArgumentException> {
                    repo.delete(emptyList())
                }
            }
            it("Given non-deleted partner Then mark as deleted") {
                val partnerNotDeleted = insertPartner { it.copy(isDeleted = false) }

                repo.delete(listOf(partnerNotDeleted.id))

                repo.select().shouldBeSingleton().first().isDeleted shouldBe true
            }
            it("Given two non-deleted partners Then mark only one as deleted") {
                val partnerToBeDeleted = insertPartner { it.copy(id = 1, isDeleted = false) }
                insertPartner { it.copy(id = 2, isDeleted = false) }

                repo.delete(listOf(partnerToBeDeleted.id))

                repo.select().filter { it.isDeleted }.shouldBeSingleton().first().also {
                    it.id shouldBe partnerToBeDeleted.id
                    it.isDeleted shouldBe true
                }
            }
        }
    }

    private fun insertPartner(mutateDomainObject: (Partner) -> Partner): Partner {
        ExposedCategoriesRepo.insertIfNotExists(listOf(category))
        val partnerWithCategory = partner.copy(categories = listOf(category)).let(mutateDomainObject)
        repo.insert(listOf(partnerWithCategory))
        return partnerWithCategory
    }
}

private fun ExposedCategoriesRepo.insertIfNotExists(categories: List<Category>) {
    transaction {
        insert(categories.filter {
            CategoryDbo.findById(it.id) == null
        })
    }
}
