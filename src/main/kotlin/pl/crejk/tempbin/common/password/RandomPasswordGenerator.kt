package pl.crejk.tempbin.common.password

import pl.crejk.tempbin.util.SecurityUtil

class RandomPasswordGenerator : PasswordGenerator {

    override fun generate(): String =
        SecurityUtil.randomString(32)
}
