package allfit.service.credentials

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EncrypterTest : StringSpec() {

    private val secret = "my secret"

    init {
        "Given default encrypter When crypt back and forth Then secret stays same" {
            Encrypter().encryptAndDecrypt() shouldBe secret
        }
        "Given extra long password When crypt back and forth Then secret stays same" {
            Encrypter { "012345689012345689012345689" }.encryptAndDecrypt() shouldBe secret
        }
    }

    private fun Encrypter.encryptAndDecrypt() = decrypt(encrypt(secret))
}
