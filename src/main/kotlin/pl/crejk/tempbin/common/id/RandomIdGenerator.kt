package pl.crejk.tempbin.common.id

import java.util.UUID

class RandomIdGenerator : IdGenerator {

    override fun generate(): String =
        UUID.randomUUID().toString()
}
