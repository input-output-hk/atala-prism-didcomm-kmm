package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.apollo.base64.base64UrlDecodedBytes
import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypePeerDID

/**
 * Converts a public key to a JSON Web Key (JWK).
 *
 * @param publicKey The public key to convert as a byte array.
 * @param verMethodType The type of verification method.
 * @return The public key converted to a JWK.
 * @throws IllegalArgumentException if the JWK type is unsupported.
 */
@Throws(IllegalArgumentException::class)
fun toJwk(publicKey: ByteArray, verMethodType: VerificationMethodTypePeerDID): Map<String, String> {
    val x = publicKey.base64UrlEncoded
    val crv =
        when (verMethodType) {
            VerificationMethodTypeAuthentication.JsonWebKey2020 -> "Ed25519"
            VerificationMethodTypeAgreement.JsonWebKey2020 -> "X25519"
            else -> throw IllegalArgumentException("Unsupported JWK type ${verMethodType.value}")
        }
    return mapOf(
        "kty" to "OKP",
        "crv" to crv,
        "x" to x
    )
}

/**
 * Converts a given JWK object to a byte array.
 * @param verMaterial the VerificationMaterialPeerDID object representing the JWK
 * @throws IllegalArgumentException if the JWK key is invalid
 * @return the JWK key as a byte array
 */
@Throws(IllegalArgumentException::class)
fun fromJwk(verMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>): ByteArray {
    val jwkDict = if (verMaterial.value is Map<*, *>) verMaterial.value else fromJsonToMap(verMaterial.value.toString())

    if (!jwkDict.containsKey("crv")) {
        throw IllegalArgumentException("Invalid JWK key - no 'crv' fields: ${verMaterial.value}")
    }
    if (!jwkDict.containsKey("x")) {
        throw IllegalArgumentException("Invalid JWK key - no 'x' fields: ${verMaterial.value}")
    }

    val crv = jwkDict["crv"]
    if (verMaterial.type is VerificationMethodTypeAuthentication && crv != "Ed25519") {
        throw IllegalArgumentException("Invalid JWK key type - authentication expected: ${verMaterial.value}")
    }
    if (verMaterial.type is VerificationMethodTypeAgreement && crv != "X25519") {
        throw IllegalArgumentException("Invalid JWK key type - key agreement expected: ${verMaterial.value}")
    }

    val value = jwkDict["x"].toString()
    return value.base64UrlDecodedBytes
}
