package io.iohk.atala.prism.mercury.didpeer.core

import com.ditchoom.buffer.PlatformBuffer

object VarInt {
    fun writeVarInt(value: Int, byteBuffer: PlatformBuffer) {
        var value = value
        byteBuffer.resetForWrite()
        while ((value and -0x80).toLong() != 0L) {
            byteBuffer.write((value and 0x7F or 0x80).toByte())
            // byteBuffer.put((value and 0x7F or 0x80).toByte())
            value = value ushr 7
        }
        byteBuffer.write((value and 0x7F).toByte())
        // byteBuffer.put((value and 0x7F).toByte())
    }

    fun readVarint(byteBuffer: PlatformBuffer): Int {
        var value = 0
        var i = 0
        var b = 0
        byteBuffer.resetForRead()
        while (byteBuffer.remaining() > 0
            && byteBuffer.readInt().also { b = it } and 0x80 != 0
        ) {
            value = value or (b and 0x7F shl i)
            i += 7
            if (i > 35) {
                throw IllegalArgumentException("Variable length quantity is too long")
            }
        }
        value = value or (b shl i)
        return value
    }
}
