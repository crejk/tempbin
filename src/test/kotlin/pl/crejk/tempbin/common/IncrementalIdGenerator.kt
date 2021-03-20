package pl.crejk.tempbin.common

import java.util.concurrent.atomic.AtomicInteger
import pl.crejk.tempbin.common.id.IdGenerator

internal class IncrementalIdGenerator : IdGenerator {

    private val generator = AtomicInteger(1)

    override fun generate(): String =
        "${this.generator.getAndIncrement()}"
}
