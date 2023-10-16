package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.apollo.base58.base58BtcDecodedBytes
import io.iohk.atala.prism.apollo.base58.base58BtcEncoded
import io.iohk.atala.prism.apollo.multibase.MultiBase

fun toBase58Multibase(value: ByteArray) =
    MultiBase.encode(MultiBase.Base.BASE58_BTC, value)

fun toBase58(value: ByteArray) = value.base58BtcEncoded

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

fun fromBase58(value: String): ByteArray {
    if (!isBase58(value)) {
        throw IllegalArgumentException("Invalid key: Invalid base58 encoding: $value")
    }
    return value.base58BtcDecodedBytes
}

fun isBase58(value: String): Boolean {
    val alphabet = Regex("[1-9a-km-zA-HJ-NP-Z]+")
    return alphabet.matches(value)
}
