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

@Serializable
data class DIDDocPeerDID
@JvmOverloads
constructor(
    val did: String,
    val authentication: List<VerificationMethodPeerDID>,
    val keyAgreement: List<VerificationMethodPeerDID> = emptyList(),
    val service: List<Service>? = null
) {
    val authenticationKids
        get() = authentication.map { it.id }
    val agreementKids
        get() = keyAgreement.map { it.id }

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

@Serializable
data class VerificationMethodPeerDID(
    val id: String,
    val controller: String,
    val verMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>
) {
    private fun publicKeyField() =
        when (verMaterial.format) {
            VerificationMaterialFormatPeerDID.BASE58 -> PublicKeyField.BASE58
            VerificationMaterialFormatPeerDID.JWK -> PublicKeyField.JWK
            VerificationMaterialFormatPeerDID.MULTIBASE -> PublicKeyField.MULTIBASE
        }

    fun toDict() =
        mapOf(
            "id" to id,
            "type" to verMaterial.type.value,
            "controller" to controller,
            publicKeyField().value to verMaterial.value
        )
}

sealed interface Service

data class OtherService(val data: Map<String, Any>) : Service

data class DIDCommServicePeerDID(
    val id: String,
    val type: String,
    val serviceEndpoint: String,
    val routingKeys: List<String>,
    val accept: List<String>
) : Service {
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

enum class PublicKeyField(val value: String) {
    BASE58("publicKeyBase58"),
    MULTIBASE("publicKeyMultibase"),
    JWK("publicKeyJwk")
}
