package pl.crejk.tempbin.common.password

import pl.crejk.tempbin.common.SecurityUtil

class RandomPasswordGenerator : PasswordGenerator {

    override fun generate(): String =
        SecurityUtil.generateRandomString(32)
}
