package io.iohk.atala.prism.mercury.didpeer.core

import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypePeerDID
import okio.Buffer

enum class Codec(val prefix: Int) {
    X25519(0xEC),
    ED25519(0xED);
}

fun toMulticodec(value: ByteArray, keyType: VerificationMethodTypePeerDID): ByteArray {
    val prefix = getCodec(keyType).prefix
    val byteBuffer = Buffer()
    VarInt.writeVarInt(prefix, byteBuffer)
    return byteBuffer.array().plus(value)
}

fun fromMulticodec(value: ByteArray): Pair<Codec, ByteArray> {
    Buffer.UnsafeCursor
    val prefix = VarInt.readVarInt(Buffer.wrap(value))
    val codec = getCodec(prefix)
    val byteBuffer = ByteBufferNativeType.allocate(2)
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
