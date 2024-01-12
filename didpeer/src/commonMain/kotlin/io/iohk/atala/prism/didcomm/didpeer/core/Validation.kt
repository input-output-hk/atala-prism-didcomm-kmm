package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypePeerDID
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Validates the type of authentication material.
 *
 * @param verificationMaterial The verification material to validate.
 * @Throws IllegalArgumentException if the verification material type is not
 *     VerificationMethodTypeAuthentication.
 */
@Throws(IllegalArgumentException::class)
internal fun validateAuthenticationMaterialType(verificationMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>) {
    if (verificationMaterial.type !is VerificationMethodTypeAuthentication) {
        throw IllegalArgumentException(
            "Invalid verification material type: ${verificationMaterial.type} instead of VerificationMaterialAuthentication"
        )
    }
}

/**
 * Validates the agreement material type of the verification material.
 *
 * @param verificationMaterial The verification material to be validated.
 * @throws IllegalArgumentException If the verification material type is not a subclass of VerificationMethodTypeAgreement.
 */
@Throws(IllegalArgumentException::class)
internal fun validateAgreementMaterialType(verificationMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>) {
    if (verificationMaterial.type !is VerificationMethodTypeAgreement) {
        throw IllegalArgumentException(
            "Invalid verification material type: ${verificationMaterial.type} instead of VerificationMaterialAgreement"
        )
    }
}

/**
 * Validates a JSON string.
 *
 * @param value The JSON string to validate
 * @throws IllegalArgumentException If the JSON string is invalid
 */
@Throws(IllegalArgumentException::class)
internal fun validateJson(value: String) {
    try {
        Json.parseToJsonElement(value)
    } catch (ex: SerializationException) {
        throw IllegalArgumentException("Invalid JSON $value", ex)
    }
    if (!value.contains("{")) throw IllegalArgumentException("Invalid JSON $value")
}

/**
 * Validates the length of a raw key byte array.
 *
 * @param key The raw key byte array.
 * @throws IllegalArgumentException If the length of the raw key is not equal to 32.
 */
@Throws(IllegalArgumentException::class)
internal fun validateRawKeyLength(key: ByteArray) {
    // for all supported key types now (ED25519 and X25510) the expected size is 32
    if (key.size != 32) {
        throw IllegalArgumentException("Invalid key $key, size should be 32 and it is: ${key.size}")
    }
}
