package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.didcomm.didpeer.varint.VarInt
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

class VarIntTests {
    @Test
    fun testVarIntX25519() {
        val origin = Codec.X25519.prefix
        val byteBuffer = Buffer()
        VarInt.write(origin, byteBuffer)
        val fin: Int = VarInt.read(byteBuffer)
        assertEquals(origin, fin)
    }

    @Test
    fun testVarIntED25519() {
        val origin = Codec.ED25519.prefix
        val byteBuffer = Buffer()
        VarInt.write(origin, byteBuffer)
        val fin: Int = VarInt.read(byteBuffer)
        assertEquals(origin, fin)
    }
}
