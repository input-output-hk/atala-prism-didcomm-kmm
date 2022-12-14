package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypePeerDID
import okio.Buffer

enum class Codec(val prefix: Int) {
    X25519(0xEC),
    ED25519(0xED);
}

fun toMulticodec(value: ByteArray, keyType: VerificationMethodTypePeerDID): ByteArray {
    val prefix = getCodec(keyType).prefix
    val byteBuffer = Buffer()
    VarInt.writeVarInt(prefix, byteBuffer)
    return byteBuffer.readByteArray().plus(value)
}

fun fromMulticodec(value: ByteArray): Pair<Codec, ByteArray> {
    val prefix = VarInt.readVarInt(Buffer().write(value))
    val codec = getCodec(prefix)
    val byteBuffer = Buffer()
    VarInt.writeVarInt(prefix, byteBuffer)
    return Pair(codec, value.drop(2).toByteArray())
}

private fun getCodec(keyType: VerificationMethodTypePeerDID) =
    when (keyType) {
        is VerificationMethodTypeAuthentication -> Codec.ED25519
        is VerificationMethodTypeAgreement -> Codec.X25519
    }

private fun getCodec(prefix: Int) =
    Codec.values().find { it.prefix == prefix }
        ?: throw IllegalArgumentException("Invalid key: Prefix $prefix not supported")
