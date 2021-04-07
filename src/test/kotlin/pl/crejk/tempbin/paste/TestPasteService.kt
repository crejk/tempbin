package pl.crejk.tempbin.paste

import pl.crejk.tempbin.TimeProvider
import pl.crejk.tempbin.common.FakePasswordGenerator
import pl.crejk.tempbin.common.IncrementalIdGenerator
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

internal fun testPasteService(maxContentLength: Int = 100, timeProvider: TimeProvider): PasteService =
    PasteService(
        InMemoryPasteRepo(),
        PasteCreator(IncrementalIdGenerator(), FakePasswordGenerator(), maxContentLength, timeProvider),
        timeProvider
    )
