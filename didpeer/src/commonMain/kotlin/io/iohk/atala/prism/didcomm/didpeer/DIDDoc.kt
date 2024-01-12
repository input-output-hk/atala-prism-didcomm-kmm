package io.iohk.atala.prism.didcomm.didpeer

import io.iohk.atala.prism.didcomm.didpeer.core.didDocFromJson
import io.iohk.atala.prism.didcomm.didpeer.core.toJsonElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.jvm.JvmOverloads

const val SERVICE_ID = "id"
const val SERVICE_TYPE = "type"
const val SERVICE_ENDPOINT = "serviceEndpoint"
const val SERVICE_DIDCOMM_MESSAGING = "DIDCommMessaging"
const val SERVICE_ROUTING_KEYS = "routingKeys"
const val SERVICE_ACCEPT = "accept"

/**
 * Represents a PeerDID DID Document.
 *
 * @property did The DID identifier.
 * @property authentication The list of authentication verification methods.
 * @property keyAgreement The list of key agreement verification methods.
 * @property service The list of service endpoints.
 */
@Serializable
data class DIDDocPeerDID
@JvmOverloads
constructor(
    val did: String,
    val authentication: List<VerificationMethodPeerDID>,
    val keyAgreement: List<VerificationMethodPeerDID> = emptyList(),
    val service: List<Service>? = null
) {
    /**
     * Retrieves a list of IDs from the authentication list.
     *
     * @return The list of authentication kid IDs.
     */
    val authenticationKids
        get() = authentication.map { it.id }

    /**
     * Represents the agreementKids property in the DIDDocPeerDID class.
     * It is a read-only property that returns a list of IDs of key agreements in the DID document.
     *
     * @return The list of IDs of key agreements.
     */
    val agreementKids
        get() = keyAgreement.map { it.id }

    /**
     * Converts the DID document to a dictionary representation.
     * @return The dictionary representation of the DID document.
     */
    fun toDict(): Map<String, Any> {
        val res =
            mutableMapOf(
                "id" to did,
                "authentication" to authentication.map { it.toDict() }
            )
        if (keyAgreement.isNotEmpty()) {
            res["keyAgreement"] = keyAgreement.map { it.toDict() }
        }
        service?.let {
            res["service"] =
                service.map {
                    when (it) {
                        is OtherService -> it.data
                        is DIDCommServicePeerDID -> it.toDict()
                    }
                }
        }
        return res
    }

    /**
     * Converts the object to its JSON representation as a String.
     *
     * @return The JSON representation of the object as a String.
     */
    fun toJson(): String {
        return toDict().toJsonElement().toString()
    }

    companion object {
        /**
         * Creates a new instance of DIDDocPeerDID from the given DID Doc JSON.
         *
         * @param value DID Doc JSON
         * @throws MalformedPeerDIDDOcException if the input DID Doc JSON is not a valid peerdid DID Doc
         * @return [DIDDocPeerDID] instance
         */
        @Throws(MalformedPeerDIDDOcException::class)
        fun fromJson(value: JSON): DIDDocPeerDID {
            try {
                // Two ways
                return didDocFromJson(Json.parseToJsonElement(value).jsonObject)
            } catch (e: Exception) {
                throw MalformedPeerDIDDOcException(e)
            }
        }
    }
}

/**
 * Represents a verification method used in PeerDID.
 *
 * @property id The ID of the verification method.
 * @property controller The controller of the verification method.
 * @property verMaterial The verification material of the verification method.
 */
@Serializable
data class VerificationMethodPeerDID(
    val id: String,
    val controller: String,
    val verMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>
) {
    /**
     * Returns the appropriate public key field based on the verification material format.
     *
     * @return The public key field.
     */
    private fun publicKeyField() =
        when (verMaterial.format) {
            VerificationMaterialFormatPeerDID.BASE58 -> PublicKeyField.BASE58
            VerificationMaterialFormatPeerDID.JWK -> PublicKeyField.JWK
            VerificationMaterialFormatPeerDID.MULTIBASE -> PublicKeyField.MULTIBASE
        }

    /**
     * Converts the VerificationMethodPeerDID to a dictionary representation.
     *
     * @return The dictionary representation of the VerificationMethodPeerDID.
     */
    fun toDict() =
        mapOf(
            "id" to id,
            "type" to verMaterial.type.value,
            "controller" to controller,
            publicKeyField().value to verMaterial.value
        )
}

/**
 * Represents a service.
 */
sealed interface Service

/**
 * Represents a service provided by a DID document.
 *
 * @property data The data of the service.
 */
data class OtherService(val data: Map<String, Any>) : Service

/**
 * Represents a DIDComm service peer DID.
 * @property id The ID of the service.
 * @property type The type of the service.
 * @property serviceEndpoint The service endpoint.
 * @property routingKeys The list of routing keys.
 * @property accept The list of accepted message types.
 */
data class DIDCommServicePeerDID(
    val id: String,
    val type: String,
    val serviceEndpoint: String,
    val routingKeys: List<String>,
    val accept: List<String>
) : Service {
    /**
     * Converts the DIDCommServicePeerDID object to a mutable map representation.
     *
     * @return The mutable map representation of the DIDCommServicePeerDID object.
     */
    fun toDict(): MutableMap<String, Any> {
        val res =
            mutableMapOf<String, Any>(
                SERVICE_ID to id,
                SERVICE_TYPE to type
            )
        res[SERVICE_ENDPOINT] = serviceEndpoint
        res[SERVICE_ROUTING_KEYS] = routingKeys
        res[SERVICE_ACCEPT] = accept
        return res
    }
}

/**
 * Represents the different types of public key fields.
 *
 * @property value The string value of the public key field.
 */
enum class PublicKeyField(val value: String) {
    BASE58("publicKeyBase58"),
    MULTIBASE("publicKeyMultibase"),
    JWK("publicKeyJwk")
}
