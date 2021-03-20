package pl.crejk.tempbin.util

import java.security.SecureRandom
import java.util.UUID
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor

internal object SecurityUtil {

    private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun generateSalt(): String =
        DigestUtils.sha256Hex(UUID.randomUUID().toBytes())

    fun prepareTextEncryptor(password: String, salt: String = generateSalt()): TextEncryptor =
        Encryptors.delux(password, salt)

    fun generateRandomString(length: Int): String =
        SecureRandom().let { random ->
            (1..length)
                .map { random.nextInt(chars.size) }
                .map(chars::get)
                .joinToString("")
        }
}
