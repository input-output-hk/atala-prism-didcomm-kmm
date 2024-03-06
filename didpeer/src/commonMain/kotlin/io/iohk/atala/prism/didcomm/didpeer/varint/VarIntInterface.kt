package io.iohk.atala.prism.didcomm.didpeer.varint

import okio.Buffer

interface VarIntInterface {
    fun write(value: Int, byteBuffer: Buffer)
    fun read(byteBuffer: Buffer): Int
}
