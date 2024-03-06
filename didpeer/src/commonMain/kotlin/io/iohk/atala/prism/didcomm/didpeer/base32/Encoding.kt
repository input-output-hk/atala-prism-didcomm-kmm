package io.iohk.atala.prism.didcomm.didpeer.base32

/**
 * Base32 encoding scheme
 *
 * TODO: Figure out a way to put both type with padded version in on scheme
 */
sealed interface Encoding {
    val alphabet: String

    /**
     * Base32 Standard
     */
    data object Standard : Encoding {
        override val alphabet: String = "abcdefghijklmnopqrstuvwxyz234567"
    }

    /**
     * Base32 Upper
     */
    data object Upper : Encoding {
        override val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    }

    /**
     * Base32 Hex
     */
    data object Hex : Encoding {
        override val alphabet: String = "0123456789abcdefghijklmnopqrstuvw"
    }

    /**
     * Base32 Hex Upper
     */
    data object HexUpper : Encoding {
        override val alphabet: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVW"
    }
}
