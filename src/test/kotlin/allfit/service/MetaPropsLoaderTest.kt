package allfit.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs

class MetaPropsLoaderTest : StringSpec() {
    init {
        "loading twice returns same instance" {
            val instance1 = MetaPropsLoader.instance
            val instance2 = MetaPropsLoader.instance

            instance1 shouldBeSameInstanceAs instance2
        }
    }
}
