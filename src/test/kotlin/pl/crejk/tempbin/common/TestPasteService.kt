package pl.crejk.tempbin.common

import pl.crejk.tempbin.paste.PasteService
import pl.crejk.tempbin.paste.infrastructure.InMemoryPasteRepo

internal fun testPasteService(): PasteService =
    PasteService(InMemoryPasteRepo(), IncrementalIdGenerator(), FakePasswordGenerator())
