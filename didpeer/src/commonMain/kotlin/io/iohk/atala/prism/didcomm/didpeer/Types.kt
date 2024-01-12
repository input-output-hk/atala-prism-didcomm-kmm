package io.iohk.atala.prism.didcomm.didpeer

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * An enumeration that represents the format of verification material in a PeerDID.
 */
enum class VerificationMaterialFormatPeerDID {
    JWK,
    BASE58,
    MULTIBASE
}

/**
 * Represents the types of verification methods for PeerDID.
 *
 * @param value The string value associated with the verification method type.
 */
@Serializable
sealed class VerificationMethodTypePeerDID(val value: String)

/**
 * A sealed class representing the types of verification methods for agreements.
 *
 * @property value The value associated with the verification method type.
 */
sealed class VerificationMethodTypeAgreement(value: String) : VerificationMethodTypePeerDID(value) {
    object JsonWebKey2020 : VerificationMethodTypeAgreement("JsonWebKey2020")

    object X25519KeyAgreementKey2019 : VerificationMethodTypeAgreement("X25519KeyAgreementKey2019")

    object X25519KeyAgreementKey2020 : VerificationMethodTypeAgreement("X25519KeyAgreementKey2020")
}

/**
 * Represents the different types of authentication methods for verification.
 *
 * @param value The string value representing the authentication method type.
 */
sealed class VerificationMethodTypeAuthentication(value: String) : VerificationMethodTypePeerDID(value) {
    object JsonWebKey2020 : VerificationMethodTypeAuthentication("JsonWebKey2020")

    object ED25519VerificationKey2018 : VerificationMethodTypeAuthentication("Ed25519VerificationKey2018")

    object ED25519VerificationKey2020 : VerificationMethodTypeAuthentication("Ed25519VerificationKey2020")
}

/**
 * Represents the verification material used in a PeerDID.
 *
 * @param T The type of the verification method.
 * @property format The format of the verification material.
 * @property value The value of the verification material.
 * @property type The type of the verification method.
 */
@Serializable
data class VerificationMaterialPeerDID<T : VerificationMethodTypePeerDID>(
    val format: VerificationMaterialFormatPeerDID,
    @Contextual val value: Any,
    val type: T
)

/**
 * Alias for [VerificationMaterialPeerDID] with [VerificationMethodTypeAgreement] type parameter.
 */
typealias VerificationMaterialAgreement = VerificationMaterialPeerDID<VerificationMethodTypeAgreement>

/**
 * Alias for [VerificationMaterialPeerDID] with [VerificationMethodTypeAuthentication] type parameter.
 */
typealias VerificationMaterialAuthentication = VerificationMaterialPeerDID<VerificationMethodTypeAuthentication>

/**
 * Type alias for JSON strings.
 */
typealias JSON = String

/**
 * Defines the type PeerDID, which is an alias for a String.
 */
typealias PeerDID = String
