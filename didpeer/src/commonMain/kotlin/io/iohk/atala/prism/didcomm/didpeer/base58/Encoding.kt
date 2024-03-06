package io.iohk.atala.prism.didcomm.didpeer.base58

/**
 * Base58 encoding scheme
 */
sealed interface Encoding {
    val alphabet: String

    /**
     * Base58 BTC => Standard
     */
    data object BTC : Encoding {
        override val alphabet: String = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    }

    /**
     * Base58 Flickr
     */
    data object Flickr : Encoding {
        override val alphabet: String = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ"
    }
}
