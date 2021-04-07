package pl.crejk.tempbin.common

import java.nio.ByteBuffer
import java.util.UUID

private const val UUID_BYTE_SIZE = 16

internal fun UUID.toBytes(): ByteArray =
    ByteBuffer.wrap(ByteArray(UUID_BYTE_SIZE)).let {
        it.putLong(this.mostSignificantBits)
        it.putLong(this.leastSignificantBits)

        it.array()
    }
