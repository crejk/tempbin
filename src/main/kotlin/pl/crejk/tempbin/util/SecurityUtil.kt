package pl.crejk.tempbin.util

import org.apache.commons.codec.digest.DigestUtils
import org.springframework.security.crypto.encrypt.Encryptors
import org.springframework.security.crypto.encrypt.TextEncryptor
import java.util.*

object SecurityUtil {

    fun generateId(): String =
        DigestUtils.sha1Hex(UUID.randomUUID().toBytes())

    fun generatePassword(): String =
        StringUtil.randomString(32)

    fun generateSalt(): String =
        DigestUtils.sha256Hex(UUID.randomUUID().toBytes())

    fun prepareTextEncryptor(password: String = generatePassword(), salt: String = generateSalt()): TextEncryptor =
        Encryptors.text(password, salt)
}