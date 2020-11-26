package pl.crejk.tempbin.util

import org.apache.commons.codec.digest.DigestUtils
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import java.util.*

internal object SecurityUtil {

    fun generateSalt(): String =
        DigestUtils.sha256Hex(UUID.randomUUID().toBytes())

    fun prepareTextEncryptor(password: String, salt: String = generateSalt()): TextEncryptor =
        Encryptors.delux(password, salt)
}