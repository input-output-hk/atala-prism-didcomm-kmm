package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypePeerDID
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal fun validateAuthenticationMaterialType(verificationMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>) {
    if (verificationMaterial.type !is VerificationMethodTypeAuthentication)
        throw IllegalArgumentException("Invalid verification material type: ${verificationMaterial.type} instead of VerificationMaterialAuthentication")
}

internal fun validateAgreementMaterialType(verificationMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>) {
    if (verificationMaterial.type !is VerificationMethodTypeAgreement)
        throw IllegalArgumentException("Invalid verification material type: ${verificationMaterial.type} instead of VerificationMaterialAgreement")
}

internal fun validateJson(value: String) {
    try {
        Json.parseToJsonElement(value)
    } catch (ex: SerializationException) {
        throw IllegalArgumentException("Invalid JSON $value", ex)
    }
    if (!value.contains("{")) throw IllegalArgumentException("Invalid JSON $value")
}

internal fun validateRawKeyLength(key: ByteArray) {
    // for all supported key types now (ED25519 and X25510) the expected size is 32
    if (key.size != 32)
        throw IllegalArgumentException("Invalid key $key")
}
