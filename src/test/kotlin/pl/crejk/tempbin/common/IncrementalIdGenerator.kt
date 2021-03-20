package pl.crejk.tempbin.common

import pl.crejk.tempbin.common.id.IdGenerator
import java.util.concurrent.atomic.AtomicInteger

internal class IncrementalIdGenerator : IdGenerator {

    private val generator = AtomicInteger(1)

    override fun generate(): String =
        "${generator.getAndIncrement()}"
}
