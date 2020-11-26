package pl.crejk.tempbin.paste

import pl.crejk.tempbin.common.password.PasswordGenerator

internal class FakePasswordGenerator : PasswordGenerator {

    override fun generate(): String =
        "password"
}
