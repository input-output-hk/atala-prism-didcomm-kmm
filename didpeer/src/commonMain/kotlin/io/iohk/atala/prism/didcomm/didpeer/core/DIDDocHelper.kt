package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.didcomm.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.didcomm.didpeer.DIDDocPeerDID
import io.iohk.atala.prism.didcomm.didpeer.OtherService
import io.iohk.atala.prism.didcomm.didpeer.PublicKeyField
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_DIDCOMM_MESSAGING
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_ENDPOINT
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_ID
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_TYPE
import io.iohk.atala.prism.didcomm.didpeer.Service
import io.iohk.atala.prism.didcomm.didpeer.ServiceEndpoint
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypePeerDID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Maps verification method types to corresponding public key fields.
 */
private val verTypeToField =
    mapOf(
        VerificationMethodTypeAgreement.X25519KeyAgreementKey2019 to PublicKeyField.BASE58,
        VerificationMethodTypeAgreement.X25519KeyAgreementKey2020 to PublicKeyField.MULTIBASE,
        VerificationMethodTypeAgreement.JsonWebKey2020 to PublicKeyField.JWK,
        VerificationMethodTypeAuthentication.ED25519VerificationKey2018 to PublicKeyField.BASE58,
        VerificationMethodTypeAuthentication.ED25519VerificationKey2020 to PublicKeyField.MULTIBASE,
        VerificationMethodTypeAuthentication.JsonWebKey2020 to PublicKeyField.JWK
    )

/**
 * Mapping from verification method type to verification material format.
 */
private val verTypeToFormat =
    mapOf(
        VerificationMethodTypeAgreement.X25519KeyAgreementKey2019 to VerificationMaterialFormatPeerDID.BASE58,
        VerificationMethodTypeAgreement.X25519KeyAgreementKey2020 to VerificationMaterialFormatPeerDID.MULTIBASE,
        VerificationMethodTypeAgreement.JsonWebKey2020 to VerificationMaterialFormatPeerDID.JWK,
        VerificationMethodTypeAuthentication.ED25519VerificationKey2018 to VerificationMaterialFormatPeerDID.BASE58,
        VerificationMethodTypeAuthentication.ED25519VerificationKey2020 to VerificationMaterialFormatPeerDID.MULTIBASE,
        VerificationMethodTypeAuthentication.JsonWebKey2020 to VerificationMaterialFormatPeerDID.JWK
    )

/**
 * Creates a [DIDDocPeerDID] instance from a JSON object.
 *
 * @param jsonObject The JSON object representing the DID Doc.
 * @return [DIDDocPeerDID] instance.
 * @throws IllegalArgumentException if the JSON object is missing required fields.
 */
@Throws(IllegalArgumentException::class)
internal fun didDocFromJson(jsonObject: JsonObject): DIDDocPeerDID {
    val did =
        jsonObject["id"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("No 'id' field")
    val authentication =
        jsonObject["authentication"]
            ?.jsonArray
            ?.map { verificationMethodFromJson(it.jsonObject) }
            ?: emptyList()
    val keyAgreement =
        jsonObject["keyAgreement"]
            ?.jsonArray
            ?.map { verificationMethodFromJson(it.jsonObject) }
            ?: emptyList()
    val service =
        jsonObject["service"]
            ?.jsonArray
            ?.map { serviceFromJson(it.jsonObject) }
    return DIDDocPeerDID(
        did = did,
        authentication = authentication,
        keyAgreement = keyAgreement,
        service = service
    )
}

/**
 * Converts a JSON object to a VerificationMethodPeerDID object.
 *
 * @param jsonObject The JSON object representing the verification method.
 * @return A VerificationMethodPeerDID object.
 * @throws IllegalArgumentException if the required fields are missing in the JSON object.
 */
@Suppress("IMPLICIT_CAST_TO_ANY")
@Throws(IllegalArgumentException::class)
internal fun verificationMethodFromJson(jsonObject: JsonObject): VerificationMethodPeerDID {
    val id =
        jsonObject["id"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("No 'id' field in method $jsonObject")
    val controller =
        jsonObject["controller"]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("No 'controller' field in method $jsonObject")

    val verMaterialType = getVerMethodType(jsonObject)
    val field = verTypeToField.getValue(verMaterialType)
    val format = verTypeToFormat.getValue(verMaterialType)
    val value =
        if (verMaterialType is VerificationMethodTypeAgreement.JsonWebKey2020 ||
            verMaterialType is VerificationMethodTypeAuthentication.JsonWebKey2020
        ) {
            val jwkJson =
                jsonObject[field.value]?.jsonObject
                    ?: throw IllegalArgumentException("No 'field' field in method $jsonObject")
            jwkJson.toMap()
        } else {
            jsonObject[field.value]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("No 'field' field in method $jsonObject")
        }

    return VerificationMethodPeerDID(
        id = id,
        controller = controller,
        verMaterial =
        VerificationMaterialPeerDID(
            format = format,
            type = verMaterialType,
            value = value
        )
    )
}

/**
 * Parses a JSON object into a Service object.
 *
 * @param jsonObject The JSON object to parse.
 * @return The parsed Service object.
 * @throws IllegalArgumentException if the 'id' or 'type' field is missing in the JSON object.
 */
@Throws(IllegalArgumentException::class)
internal fun serviceFromJson(jsonObject: JsonObject): Service {
    val serviceMap = jsonObject.toMap()

    val id =
        jsonObject[SERVICE_ID]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("No 'id' field in service $jsonObject")
    val type =
        jsonObject[SERVICE_TYPE]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("No 'type' field in service $jsonObject")

    if (type != SERVICE_DIDCOMM_MESSAGING) {
        return OtherService(serviceMap)
    }

    val serviceEndpointObject = jsonObject[SERVICE_ENDPOINT]?.jsonObject
    val uri = serviceEndpointObject?.get("uri")?.jsonPrimitive?.content ?: ""
    val routingKeys = serviceEndpointObject?.get("routingKeys")?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
    val accept = serviceEndpointObject?.get("accept")?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

    val serviceEndpoint = ServiceEndpoint(
        uri = uri,
        routingKeys = routingKeys,
        accept = accept
    )

    return DIDCommServicePeerDID(
        id = id,
        type = type,
        serviceEndpoint = serviceEndpoint
    )
}

/**
 * Retrieves the verification method type from the given JSON object.
 *
 * @param jsonObject The JSON object representing the verification method.
 * @return The corresponding VerificationMethodTypePeerDID.
 * @throws IllegalArgumentException If the 'type' field is missing in the method or if the verification
 * method type is unknown.
 */
@Throws(IllegalArgumentException::class)
private fun getVerMethodType(jsonObject: JsonObject): VerificationMethodTypePeerDID {
    val type =
        (jsonObject["type"] as JsonPrimitive).contentOrNull
            ?: throw IllegalArgumentException("No 'type' field in method $jsonObject")

    return when (type) {
        VerificationMethodTypeAgreement.X25519KeyAgreementKey2019.value
        -> VerificationMethodTypeAgreement.X25519KeyAgreementKey2019

        VerificationMethodTypeAgreement.X25519KeyAgreementKey2020.value
        -> VerificationMethodTypeAgreement.X25519KeyAgreementKey2020

        VerificationMethodTypeAuthentication.ED25519VerificationKey2018.value
        -> VerificationMethodTypeAuthentication.ED25519VerificationKey2018

        VerificationMethodTypeAuthentication.ED25519VerificationKey2020.value
        -> VerificationMethodTypeAuthentication.ED25519VerificationKey2020

        VerificationMethodTypeAuthentication.JsonWebKey2020.value -> {
            val v =
                jsonObject[PublicKeyField.JWK.value]?.jsonObject
                    ?: throw IllegalArgumentException("No 'field' field in method $jsonObject")
            val crv =
                v["crv"]?.toString()
                    ?: throw IllegalArgumentException("No 'crv' field in method $jsonObject")
            if (crv == "X25519") VerificationMethodTypeAgreement.JsonWebKey2020 else VerificationMethodTypeAuthentication.JsonWebKey2020
        }

        else ->
            throw IllegalArgumentException("Unknown verification method type $type")
    }
}
