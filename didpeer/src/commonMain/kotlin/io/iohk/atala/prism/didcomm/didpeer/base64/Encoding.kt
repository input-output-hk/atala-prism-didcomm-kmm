package io.iohk.atala.prism.didcomm.didpeer.base64

/**
 * Base64 encoding scheme
 */
sealed interface Encoding {
    val alphabet: String

    /**
     * Base64 Standard
     */
    data object Standard : Encoding {
        override val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    }

    /**
     * Base64 Standard
     */
    data object StandardPad : Encoding {
        override val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
    }

    /**
     * Base64 URL
     */
    data object UrlSafe : Encoding {
        override val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
    }

    /**
     * Base64 URL
     */
    data object UrlSafePad : Encoding {
        override val alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_="
    }
}
