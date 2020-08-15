package pl.crejk.tempbin.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import pl.crejk.tempbin.util.SecurityUtil

class SecurityUtilTest : FunSpec({
    val encryptor = SecurityUtil.prepareTextEncryptor()

    val text = "abc"
    val encryptedText = encryptor.encrypt(text)
    val decryptedText = encryptor.decrypt(encryptedText)

    decryptedText shouldBe text
})
