package pl.crejk.tempbin.util

import java.nio.ByteBuffer
import java.util.*

private const val UUID_BYTE_SIZE = 16

internal fun UUID.toBytes(): ByteArray =
    ByteBuffer.wrap(ByteArray(UUID_BYTE_SIZE)).let {
        it.putLong(this.mostSignificantBits)
        it.putLong(this.leastSignificantBits)

        return it.array()
    }
