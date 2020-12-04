package pl.crejk.tempbin.common

import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

internal fun testPasteService(maxContentLength: Int = 100): PasteService =
    PasteService(InMemoryPasteRepo(), IncrementalIdGenerator(), FakePasswordGenerator(), maxContentLength)
