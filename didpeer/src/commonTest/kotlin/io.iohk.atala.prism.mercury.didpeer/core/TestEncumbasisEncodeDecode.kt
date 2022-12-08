package io.iohk.atala.prism.mercury.didpeer.core

import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypePeerDID
import kotlin.test.Test
import kotlin.test.assertEquals

internal data class DecodeEncumbasisTestData(
    val inputMultibase: String,
    val format: VerificationMaterialFormatPeerDID,
    val expected: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>
)

internal class TestEncumbasisEncodeDecode {

    val testData: List<DecodeEncumbasisTestData> = listOf(
        DecodeEncumbasisTestData(
            "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
            VerificationMaterialFormatPeerDID.BASE58,
            VerificationMaterialAuthentication(
                format = VerificationMaterialFormatPeerDID.BASE58,
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                value = "ByHnpUCFb1vAfh9CFZ8ZkmUZguURW8nSw889hy6rD8L7",
            )
        ),
        DecodeEncumbasisTestData(
            "z6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc",
            VerificationMaterialFormatPeerDID.BASE58,
            VerificationMaterialAgreement(
                format = VerificationMaterialFormatPeerDID.BASE58,
                type = VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019,
                value = "JhNWeSVLMYccCk7iopQW4guaSJTojqpMEELgSLhKwRr",
            ),
        ),
        DecodeEncumbasisTestData(
            "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
            VerificationMaterialFormatPeerDID.MULTIBASE,
            VerificationMaterialAuthentication(
                format = VerificationMaterialFormatPeerDID.MULTIBASE,
                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                value = "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
            ),
        ),
        DecodeEncumbasisTestData(
            "z6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc",
            VerificationMaterialFormatPeerDID.MULTIBASE,
            VerificationMaterialAgreement(
                format = VerificationMaterialFormatPeerDID.MULTIBASE,
                type = VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020,
                value = "z6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc",
            ),
        ),
        DecodeEncumbasisTestData(
            "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
            VerificationMaterialFormatPeerDID.JWK,
            VerificationMaterialAuthentication(
                format = VerificationMaterialFormatPeerDID.JWK,
                type = VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                value = mapOf(
                    "kty" to "OKP",
                    "crv" to "Ed25519",
                    "x" to "owBhCbktDjkfS6PdQddT0D3yjSitaSysP3YimJ_YgmA",
                )
            ),
        ),
        DecodeEncumbasisTestData(
            "z6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc",
            VerificationMaterialFormatPeerDID.JWK,
            VerificationMaterialAgreement(
                format = VerificationMaterialFormatPeerDID.JWK,
                type = VerificationMethodTypeAgreement.JSON_WEB_KEY_2020,
                value = mapOf(
                    "kty" to "OKP",
                    "crv" to "X25519",
                    "x" to "BIiFcQEn3dfvB2pjlhOQQour6jXy9d5s2FKEJNTOJik",
                ),
            ),
        )
    )

    @Test
    fun testDecodeEncumbasis() {
        for (data in testData) {
            assertEquals(
                data.expected,
                decodeMultibaseEncnumbasis(data.inputMultibase, data.format).verMaterial
            )
        }
    }
}
