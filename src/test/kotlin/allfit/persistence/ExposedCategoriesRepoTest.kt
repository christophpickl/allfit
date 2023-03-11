package allfit.persistence

import allfit.domain.Category
import allfit.domain.category
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedCategoriesRepoTest : DescribeSpec() {

    private val repo = ExposedCategoriesRepo
    private val category = Arb.category().next()

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.select()
            }
        }
        describe("When insert") {
            it("Then persisted") {
                repo.insert(listOf(category))

                repo.select().shouldBeSingleton().first() shouldBe category
            }
        }
        describe("When delete") {
            it("Given nothing Then fail") {
                shouldThrow<IllegalArgumentException> {
                    repo.delete(emptyList())
                }
            }
            it("Given non-deleted category Then mark as deleted") {
                val categoryNotDeleted = insertCategory { it.copy(isDeleted = false) }

                repo.delete(listOf(categoryNotDeleted.id))

                repo.select().shouldBeSingleton().first().isDeleted shouldBe true
            }
            it("Given two non-deleted categories Then mark only one as deleted") {
                val categoryToBeDeleted = insertCategory { it.copy(id = 1, isDeleted = false) }
                insertCategory { it.copy(id = 2, isDeleted = false) }

                repo.delete(listOf(categoryToBeDeleted.id))

                repo.select().filter { it.isDeleted }.shouldBeSingleton().first().also {
                    it.id shouldBe categoryToBeDeleted.id
                    it.isDeleted shouldBe true
                }
            }
        }
    }

    private fun insertCategory(code: (Category) -> Category): Category {
        val categoryToBeInserted = category.let(code)
        repo.insert(listOf(categoryToBeInserted))
        return categoryToBeInserted
    }
}
