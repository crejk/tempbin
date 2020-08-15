package pl.crejk.tempbin.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldNotBe

class StringUtilTest : FunSpec({
    test("random string should not be same like other random string") {
        StringUtil.randomString(STRING_LENGTH) shouldNotBe StringUtil.randomString(
            STRING_LENGTH
        )
    }
}) {

    companion object {

        private const val STRING_LENGTH = 11
        private const val HEX_LENGTH = 11
    }
}
