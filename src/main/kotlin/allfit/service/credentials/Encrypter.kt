package allfit.service.credentials

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

fun interface SecretProvider {
    fun provide(): String
}

object UsernameAsSecretProvider : SecretProvider {
    override fun provide(): String = System.getProperty("user.name")
}

class Encrypter(secretProvider: SecretProvider = UsernameAsSecretProvider) {

    private val algorithm = "AES"
    private val aesKeyLength = 32
    private val password = "AllFitPwd_${secretProvider.provide()}".take(aesKeyLength).padEnd(aesKeyLength, 'x')

    fun encrypt(plainText: String): String {
        val encryptedBytes = cipher(Cipher.ENCRYPT_MODE).doFinal(plainText.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(cipherText: String): String {
        val decryptedBytes = cipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(cipherText))
        return String(decryptedBytes)
    }

    private fun cipher(mode: Int): Cipher = Cipher.getInstance(algorithm).apply {
        init(mode, key())
    }

    private fun key(): SecretKey = SecretKeySpec(password.toByteArray(), algorithm)
}
