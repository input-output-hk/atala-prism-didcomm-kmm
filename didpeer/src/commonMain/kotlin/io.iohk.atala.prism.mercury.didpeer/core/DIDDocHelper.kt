package io.iohk.atala.prism.mercury.didpeer.core

import io.iohk.atala.prism.mercury.didpeer.DIDCommServicePeerDID
import io.iohk.atala.prism.mercury.didpeer.DIDDocPeerDID
import io.iohk.atala.prism.mercury.didpeer.OtherService
import io.iohk.atala.prism.mercury.didpeer.PublicKeyField
import io.iohk.atala.prism.mercury.didpeer.SERVICE_ACCEPT
import io.iohk.atala.prism.mercury.didpeer.SERVICE_DIDCOMM_MESSAGING
import io.iohk.atala.prism.mercury.didpeer.SERVICE_ENDPOINT
import io.iohk.atala.prism.mercury.didpeer.SERVICE_ID
import io.iohk.atala.prism.mercury.didpeer.SERVICE_ROUTING_KEYS
import io.iohk.atala.prism.mercury.didpeer.SERVICE_TYPE
import io.iohk.atala.prism.mercury.didpeer.Service
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMaterialPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodPeerDID
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.mercury.didpeer.VerificationMethodTypePeerDID
import kotlinx.serialization.json.*

private val verTypeToField = mapOf(
    VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019 to PublicKeyField.BASE58,
    VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020 to PublicKeyField.MULTIBASE,
    VerificationMethodTypeAgreement.JSON_WEB_KEY_2020 to PublicKeyField.JWK,
    VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018 to PublicKeyField.BASE58,
    VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020 to PublicKeyField.MULTIBASE,
    VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020 to PublicKeyField.JWK,
)

private val verTypeToFormat = mapOf(
    VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019 to VerificationMaterialFormatPeerDID.BASE58,
    VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020 to VerificationMaterialFormatPeerDID.MULTIBASE,
    VerificationMethodTypeAgreement.JSON_WEB_KEY_2020 to VerificationMaterialFormatPeerDID.JWK,
    VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018 to VerificationMaterialFormatPeerDID.BASE58,
    VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020 to VerificationMaterialFormatPeerDID.MULTIBASE,
    VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020 to VerificationMaterialFormatPeerDID.JWK,
)

internal fun didDocFromJson(jsonObject: JsonObject): DIDDocPeerDID {
    val did = jsonObject["id"]?.jsonPrimitive?.content
        ?: throw IllegalArgumentException("No 'id' field")
    val authentication = jsonObject["authentication"]
        ?.jsonArray
        ?.map { verificationMethodFromJson(it.jsonObject) }
        ?: emptyList()
    val keyAgreement = jsonObject["keyAgreement"]
        ?.jsonArray
        ?.map { verificationMethodFromJson(it.jsonObject) }
        ?: emptyList()
    val service = jsonObject["service"]
        ?.jsonArray
        ?.map { serviceFromJson(it.jsonObject) }
    return DIDDocPeerDID(
        did = did,
        authentication = authentication,
        keyAgreement = keyAgreement,
        service = service
    )
}

internal fun verificationMethodFromJson(jsonObject: JsonObject): VerificationMethodPeerDID {
    val id = jsonObject["id"]?.toString()
        ?: throw IllegalArgumentException("No 'id' field in method $jsonObject")
    val controller = jsonObject["controller"]?.toString()
        ?: throw IllegalArgumentException("No 'controller' field in method $jsonObject")

    val verMaterialType = getVerMethodType(jsonObject)
    val field = verTypeToField.getValue(verMaterialType)
    val format = verTypeToFormat.getValue(verMaterialType)
    val value = if (verMaterialType is VerificationMethodTypeAgreement.JSON_WEB_KEY_2020 ||
        verMaterialType is VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020
    ) {
        val jwkJson = jsonObject[field.value]?.jsonObject?.toString()
            ?: throw IllegalArgumentException("No 'field' field in method $jsonObject")
        fromJsonToMap(jwkJson)
    } else {
        jsonObject[field.value]?.toString()
            ?: throw IllegalArgumentException("No 'field' field in method $jsonObject")
    }

    return VerificationMethodPeerDID(
        id = id, controller = controller,
        verMaterial = VerificationMaterialPeerDID(
            format = format,
            type = verMaterialType,
            value = value
        )
    )
}

internal fun serviceFromJson(jsonObject: JsonObject): Service {
    val serviceMap = fromJsonToMap(jsonObject.toString())

    val id = jsonObject[SERVICE_ID]?.toString()
        ?: throw IllegalArgumentException("No 'id' field in service $jsonObject")
    val type = jsonObject[SERVICE_TYPE]?.toString()
        ?: throw IllegalArgumentException("No 'type' field in service $jsonObject")

    if (type != SERVICE_DIDCOMM_MESSAGING)
        return OtherService(serviceMap)

    val endpoint = jsonObject[SERVICE_ENDPOINT]?.toString()
    val routingKeys = jsonObject[SERVICE_ROUTING_KEYS]?.jsonArray?.map { it.toString() }
    val accept = jsonObject[SERVICE_ACCEPT]?.jsonArray?.map { it.toString() }

    return DIDCommServicePeerDID(
        id = id,
        type = type,
        serviceEndpoint = endpoint ?: "",
        routingKeys = routingKeys ?: emptyList(),
        accept = accept ?: emptyList()
    )
}

private fun getVerMethodType(jsonObject: JsonObject): VerificationMethodTypePeerDID {
    val type = (jsonObject["type"] as JsonPrimitive)?.contentOrNull
        ?: throw IllegalArgumentException("No 'type' field in method $jsonObject")

    return when (type) {
        VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019.value
        -> VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019

        VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020.value
        -> VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020

        VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018.value
        -> VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018

        VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020.value
        -> VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020

        VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020.value -> {
            val v = jsonObject[PublicKeyField.JWK.value]?.jsonObject
                ?: throw IllegalArgumentException("No 'field' field in method $jsonObject")
            val crv = v["crv"]?.toString()
                ?: throw IllegalArgumentException("No 'crv' field in method $jsonObject")
            if (crv == "X25519") VerificationMethodTypeAgreement.JSON_WEB_KEY_2020 else VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020
        }

        else ->
            throw IllegalArgumentException("Unknown verification method type $type")
    }
}
