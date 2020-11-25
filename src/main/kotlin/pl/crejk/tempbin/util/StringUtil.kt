package pl.crejk.tempbin.util

import java.security.SecureRandom

internal object StringUtil {

    private val CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()

    fun randomString(length: Int): String {
        val generator = SecureRandom()
        val stringBuilder = StringBuilder(length)

        for (i in 0..length) {
            val randomChar = CHARS[generator.nextInt(CHARS.size)]

            stringBuilder.append(randomChar)
        }

        return stringBuilder.toString()
    }
}
