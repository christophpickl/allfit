package allfit.persistence

import allfit.persistence.domain.CategoryEntity
import allfit.persistence.domain.ExposedCategoriesRepo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedCategoriesRepoTest : DescribeSpec() {

    private val repo = ExposedCategoriesRepo
    private val category = Arb.categoryEntity().next()

    init {
        extension(DbListener())

        describe("When select") {
            it("Then succeed") {
                repo.selectAll()
            }
        }
        describe("When insert") {
            it("Then persisted") {
                repo.insertAll(listOf(category))

                repo.selectAll().shouldBeSingleton().first() shouldBe category
            }
        }
        describe("When delete") {
            it("Given nothing Then fail") {
                shouldThrow<IllegalArgumentException> {
                    repo.deleteAll(emptyList())
                }
            }
            it("Given non-deleted category Then mark as deleted") {
                val categoryNotDeleted = insertCategory { it.copy(isDeleted = false) }

                repo.deleteAll(listOf(categoryNotDeleted.id))

                repo.selectAll().shouldBeSingleton().first().isDeleted shouldBe true
            }
            it("Given two non-deleted categories Then mark only one as deleted") {
                val categoryToBeDeleted = insertCategory { it.copy(id = 1, isDeleted = false) }
                insertCategory { it.copy(id = 2, isDeleted = false) }

                repo.deleteAll(listOf(categoryToBeDeleted.id))

                repo.selectAll().filter { it.isDeleted }.shouldBeSingleton().first().also {
                    it.id shouldBe categoryToBeDeleted.id
                    it.isDeleted shouldBe true
                }
            }
        }
    }

    private fun insertCategory(code: (CategoryEntity) -> CategoryEntity): CategoryEntity {
        val categoryToBeInserted = category.let(code)
        repo.insertAll(listOf(categoryToBeInserted))
        return categoryToBeInserted
    }
}
