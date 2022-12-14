package io.iohk.atala.prism.mercury.didpeer.core

import okio.Buffer

// TODO: Move this implementation to its own Module and update it to follow these specifications
//  https://github.com/multiformats/unsigned-varint
object VarInt {
    fun writeVarInt(value: Int, byteBuffer: Buffer) {
        var value = value
        while ((value and -0x80).toLong() != 0L) {
            byteBuffer.writeByte(value and 0x7F or 0x80)
            value = value ushr 7
        }
        byteBuffer.writeByte(value and 0x7F)
    }

    fun readVarInt(byteBuffer: Buffer): Int {
        var value = 0
        var i = 0
        var b = 0
        while (byteBuffer.size > 0 && byteBuffer.readByte().toInt().also { b = it } and 0x80 != 0) {
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
