package pl.crejk.tempbin.common.password

import pl.crejk.tempbin.util.StringUtil

class RandomPasswordGenerator : PasswordGenerator {

    override fun generate(): String =
        StringUtil.randomString(32)
}
