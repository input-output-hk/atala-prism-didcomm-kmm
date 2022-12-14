package io.iohk.atala.prism.didcomm.didpeer

import io.iohk.atala.prism.didcomm.didpeer.core.toJson
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class TestCreateNumalgo0 {

    @Test
    fun testCreateNumalgo0Positive() {
        for (key in validKeys) {
            val peerDIDAlgo0 = createPeerDIDNumalgo0(key)
            assertEquals(
                "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                peerDIDAlgo0
            )
            assertTrue(isPeerDID(peerDIDAlgo0))
        }
    }

    @Test
    fun testCreateNumalgo0MalformedInceptionKeyNotBase58Encoded() {
        for (key in notBase58Keys) {
            val ex = assertFails {
                createPeerDIDNumalgo0(key)
            }
            assertTrue(ex.message!!.matches(Regex("Invalid key: Invalid base58 encoding.*")))
        }
    }

    @Test
    @Ignore // to be removed once Base64 has been fixed
    fun testCreateNumalgo0MalformedShortInceptionKey() {
        // Issue with Base64
        for (key in shortKeys) {
            val ex = assertFails {
                createPeerDIDNumalgo0(key)
            }
            assertTrue(ex.message!!.matches(Regex("Invalid key.*")))
        }
    }

    @Test
    fun testCreateNumalgo0MalformedLongInceptionKey() {
        for (key in longKeys) {
            val ex = assertFails {
                createPeerDIDNumalgo0(key)
            }
            assertTrue(ex.message!!.matches(Regex("Invalid key.*")))
        }
    }

    @Test
    fun testCreateNumalgo0MalformedEmptyInceptionKey() {
        for (key in emptyKeys) {
            val ex = assertFails {
                createPeerDIDNumalgo0(key)
            }
            val expectedError = when (key.format) {
                VerificationMaterialFormatPeerDID.BASE58 -> "Invalid key: Invalid base58 encoding.*"
                VerificationMaterialFormatPeerDID.MULTIBASE -> "Invalid key: No transform part in multibase encoding.*"
                VerificationMaterialFormatPeerDID.JWK -> "Invalid key.*"
            }
            assertTrue(ex.message!!.matches(Regex(expectedError)))
        }
    }

    @Test
    fun testCreateNumalgo0InvalidMulticodecPrefix() {
        val key = VerificationMaterialAuthentication(
            value = "z78kqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
            type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
            format = VerificationMaterialFormatPeerDID.MULTIBASE,
        )
        val ex = assertFails {
            createPeerDIDNumalgo0(key)
        }
        assertTrue(ex.message!!.matches(Regex("Invalid key: Prefix.*not supported.*")))
    }

    companion object {
        val validKeys: List<VerificationMaterialAuthentication> = listOf(
            VerificationMaterialAuthentication(
                value = "ByHnpUCFb1vAfh9CFZ8ZkmUZguURW8nSw889hy6rD8L7",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                format = VerificationMaterialFormatPeerDID.BASE58,
            ),
            VerificationMaterialAuthentication(
                value = "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                format = VerificationMaterialFormatPeerDID.MULTIBASE,
            ),
            VerificationMaterialAuthentication(
                value = mapOf(
                    "kty" to "OKP",
                    "crv" to "Ed25519",
                    "x" to "owBhCbktDjkfS6PdQddT0D3yjSitaSysP3YimJ_YgmA",
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            ),
            VerificationMaterialAuthentication(
                value = toJson(
                    mapOf(
                        "kty" to "OKP",
                        "crv" to "Ed25519",
                        "x" to "owBhCbktDjkfS6PdQddT0D3yjSitaSysP3YimJ_YgmA",
                    )
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            )
        )

        val notBase58Keys: List<VerificationMaterialAuthentication> = listOf(
            VerificationMaterialAuthentication(
                value = "x8xB2pv7cw8q1Pd0DacS",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                format = VerificationMaterialFormatPeerDID.BASE58,
            ),
            VerificationMaterialAuthentication(
                value = "zx8xB2pv7cw8q1Pd0DacS",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                format = VerificationMaterialFormatPeerDID.MULTIBASE,
            )
        )

        val shortKeys: List<VerificationMaterialAuthentication> = listOf(
            VerificationMaterialAuthentication(
                value = "ByHnpUCFb1vAfh9CFZ8ZkmUZguURW8nSw889hy6rD8",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                format = VerificationMaterialFormatPeerDID.BASE58,
            ),
            VerificationMaterialAuthentication(
                value = "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                format = VerificationMaterialFormatPeerDID.MULTIBASE,
            ),
            VerificationMaterialAuthentication(
                value = mapOf(
                    "kty" to "OKP",
                    "crv" to "Ed25519",
                    "x" to "owBhCbktDjkfS6PdQddT0D3yjSitaSysP3YimJ_Ygm",
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            ),
            VerificationMaterialAuthentication(
                value = toJson(
                    mapOf(
                        "kty" to "OKP",
                        "crv" to "Ed25519",
                        "x" to "owBhCbktDjkfS6PdQddT0D3yjSitaSysP3YimJ_Ygm",
                    )
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            )
        )

        val longKeys: List<VerificationMaterialAuthentication> = listOf(
            VerificationMaterialAuthentication(
                value = "ByHnpUCFb1vAfh9CFZ8ZkmUZguURW8nSw889hy6rD8L77",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                format = VerificationMaterialFormatPeerDID.BASE58,
            ),
            VerificationMaterialAuthentication(
                value = "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7VVV",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                format = VerificationMaterialFormatPeerDID.MULTIBASE,
            ),
            VerificationMaterialAuthentication(
                value = mapOf(
                    "kty" to "OKP",
                    "crv" to "Ed25519",
                    "x" to "owBhCbktDjkfS6PdQddT0D3yjSitaSysP3YimJ_YgmA7",
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            ),
            VerificationMaterialAuthentication(
                value = toJson(
                    mapOf(
                        "kty" to "OKP",
                        "crv" to "Ed25519",
                        "x" to "owBhCbktDjkfS6PdQddT0D3yjSitaSysP3YimJ_YgmA7",
                    )
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            )
        )

        val emptyKeys: List<VerificationMaterialAuthentication> = listOf(
            VerificationMaterialAuthentication(
                value = "",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                format = VerificationMaterialFormatPeerDID.BASE58,
            ),
            VerificationMaterialAuthentication(
                value = "",
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                format = VerificationMaterialFormatPeerDID.MULTIBASE,
            ),
            VerificationMaterialAuthentication(
                value = mapOf(
                    "kty" to "OKP",
                    "crv" to "Ed25519",
                    "x" to "",
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            ),
            VerificationMaterialAuthentication(
                value = toJson(
                    mapOf(
                        "kty" to "OKP",
                        "crv" to "Ed25519",
                        "x" to "",
                    )
                ),
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                format = VerificationMaterialFormatPeerDID.JWK,
            )
        )
    }
}
