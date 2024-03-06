package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.didcomm.didpeer.varint.VarInt
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypePeerDID
import okio.Buffer

/**
 * Enum class representing different codecs.
 *
 * @property prefix The prefix value of the codec.
 */
enum class Codec(val prefix: Int) {
    X25519(0xEC),
    ED25519(0xED)
}

/**
 * Converts the given value to a multicodec byte array using the specified keyType.
 *
 * @param value The value to convert.
 * @param keyType The keyType representing the verification method type.
 * @return The multicodec byte array.
 */
fun toMulticodec(value: ByteArray, keyType: VerificationMethodTypePeerDID): ByteArray {
    val prefix = getCodec(keyType).prefix
    val byteBuffer = Buffer()
    VarInt.write(prefix, byteBuffer)
    return byteBuffer.readByteArray().plus(value)
}

/**
 * Decodes a multicodec value to a pair of Codec and the remaining bytes.
 *
 * @param value The multicodec value to decode.
 * @return A pair of Codec and the remaining bytes.
 */
fun fromMulticodec(value: ByteArray): Pair<Codec, ByteArray> {
    val prefix = VarInt.read(Buffer().write(value))
    val codec = getCodec(prefix)
    val byteBuffer = Buffer()
    VarInt.write(prefix, byteBuffer)
    return Pair(codec, value.drop(2).toByteArray())
}

/**
 * Returns the corresponding Codec for the given [VerificationMethodTypePeerDID].
 *
 * @param keyType the key type for which to retrieve the Codec
 * @return the Codec corresponding to the given key type
 */
private fun getCodec(keyType: VerificationMethodTypePeerDID) =
    when (keyType) {
        is VerificationMethodTypeAuthentication -> Codec.ED25519
        is VerificationMethodTypeAgreement -> Codec.X25519
    }

/**
 * Retrieves the Codec based on the provided prefix.
 *
 * @param prefix The prefix value to search for.
 * @return The matching Codec entry.
 * @throws IllegalArgumentException If the prefix is not supported.
 */
@Throws(IllegalArgumentException::class)
private fun getCodec(prefix: Int) =
    Codec.entries.find { it.prefix == prefix }
        ?: throw IllegalArgumentException("Invalid key: Prefix $prefix not supported")
