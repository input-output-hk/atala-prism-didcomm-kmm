package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.apollo.base58.base58BtcDecodedBytes
import io.iohk.atala.prism.apollo.base58.base58BtcEncoded
import io.iohk.atala.prism.apollo.multibase.MultiBase

/**
 * Converts a byte array to a base58 multibase encoding.
 *
 * @param value The byte array to be encoded.
 * @return The base58 multibase encoding of the byte array.
 * @throws IllegalArgumentException If the byte array is invalid.
 */
fun toBase58Multibase(value: ByteArray) =
    MultiBase.encode(MultiBase.Base.BASE58_BTC, value)

/**
 * Converts a byte array to a Base58 encoded string.
 *
 * @param value The byte array to encode.
 * @return The Base58 encoded string.
 */
fun toBase58(value: ByteArray) = value.base58BtcEncoded

/**
 * Decodes a multibase string to a pair of the transform and the encoded number basis.
 *
 * @param multibase The multibase string to decode.
 * @throws IllegalArgumentException if the multibase string is invalid.
 * @return A pair of the encoded number basis and the decoded transform.
 */
@Throws(IllegalArgumentException::class)
fun fromBase58Multibase(multibase: String): Pair<String, ByteArray> {
    if (multibase.isEmpty()) {
        throw IllegalArgumentException("Invalid key: No transform part in multibase encoding")
    }
    val transform = multibase[0]
    if (transform != MultiBase.Base.BASE58_BTC.prefix) {
        throw IllegalArgumentException("Invalid key: Prefix $transform not supported")
    }
    val encnumbasis = multibase.drop(1)
    val decodedEncnumbasis = fromBase58(encnumbasis)
    return Pair(encnumbasis, decodedEncnumbasis)
}

/**
 * Converts a base58-encoded string to a byte array.
 *
 * @param value The base58-encoded string.
 * @throws IllegalArgumentException if the input string is not a valid base58 encoding.
 * @return The decoded byte array.
 */
@Throws(IllegalArgumentException::class)
fun fromBase58(value: String): ByteArray {
    if (!isBase58(value)) {
        throw IllegalArgumentException("Invalid key: Invalid base58 encoding: $value")
    }
    return value.base58BtcDecodedBytes
}

/**
 * Checks whether a given string is base58 encoded.
 *
 * @param value the string to be checked
 * @return true if the string is base58 encoded, false otherwise
 */
fun isBase58(value: String): Boolean {
    val alphabet = Regex("[1-9a-km-zA-HJ-NP-Z]+")
    return alphabet.matches(value)
}
