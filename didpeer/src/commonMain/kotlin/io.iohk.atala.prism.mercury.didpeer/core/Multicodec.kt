package io.iohk.atala.prism.mercury.didpeer.core

import com.ditchoom.buffer.PlatformBuffer
import com.ditchoom.buffer.allocate
import com.ditchoom.buffer.wrap
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypePeerDID

enum class Codec(val prefix: Int) {
    X25519(0xEC),
    ED25519(0xED);
}

fun toMulticodec(value: ByteArray, keyType: VerificationMethodTypePeerDID): ByteArray {
    val prefix = getCodec(keyType).prefix
    val byteBuffer = PlatformBuffer.allocate(2)
    VarInt.writeVarInt(prefix, byteBuffer)
    return byteBuffer.readByteArray(byteBuffer.position()) + value
}

fun fromMulticodec(value: ByteArray): Pair<Codec, ByteArray> {
    val prefix = VarInt.readVarint(PlatformBuffer.wrap(value))
    val codec = getCodec(prefix)
    val byteBuffer = PlatformBuffer.allocate(2)
    VarInt.writeVarInt(prefix, byteBuffer)
    return Pair(codec, value.drop(byteBuffer.position()).toByteArray())
}

private fun getCodec(keyType: VerificationMethodTypePeerDID) =
    when (keyType) {
        is VerificationMethodTypeAuthentication -> Codec.ED25519
        is VerificationMethodTypeAgreement -> Codec.X25519
    }

private fun getCodec(prefix: Int) =
    Codec.values().find { it.prefix == prefix }
        ?: throw IllegalArgumentException("Invalid key: Prefix $prefix not supported")
