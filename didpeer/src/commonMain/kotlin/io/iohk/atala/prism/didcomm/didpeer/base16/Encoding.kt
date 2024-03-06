package io.iohk.atala.prism.didcomm.didpeer.base16

/**
 * Base16 encoding scheme
 */
sealed interface Encoding {
    val alphabet: String

    /**
     * Base16 Standard
     */
    data object Standard : Encoding {
        override val alphabet: String = "0123456789abcdef"
    }

    /**
     * Base16 Upper
     */
    data object Upper : Encoding {
        override val alphabet: String = "0123456789ABCDEF"
    }
}
