package allfit.persistence

import allfit.persistence.domain.ExposedSinglesRepo
import allfit.persistence.testInfra.DbListener
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ExposedSinglesRepoTest : StringSpec() {

    private val repo = ExposedSinglesRepo

    init {
        extension(DbListener())

        "Given nothing When select notes Then return default empty" {
            repo.selectNotes() shouldBe ""
        }

        "Given updated notes When select notes Then return them" {
            repo.updateNotes("foo")

            repo.selectNotes() shouldBe "foo"
        }
    }
}