@file:JvmName("PeerDIDUtils")

package io.iohk.atala.prism.didcomm.didpeer.core

import io.iohk.atala.prism.apollo.base64.base64PadDecoded
import io.iohk.atala.prism.apollo.base64.base64UrlEncoded
import io.iohk.atala.prism.didcomm.didpeer.JSON
import io.iohk.atala.prism.didcomm.didpeer.OtherService
import io.iohk.atala.prism.didcomm.didpeer.PeerDID
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_ACCEPT
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_DIDCOMM_MESSAGING
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_ENDPOINT
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_ROUTING_KEYS
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_TYPE
import io.iohk.atala.prism.didcomm.didpeer.SERVICE_URI
import io.iohk.atala.prism.didcomm.didpeer.Service
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialFormatPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMaterialPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodPeerDID
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAgreement
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypeAuthentication
import io.iohk.atala.prism.didcomm.didpeer.VerificationMethodTypePeerDID
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlin.jvm.JvmName

/**
 * Enum class representing prefixes used in the Numalgo2 algorithm.
 * Each prefix is associated with a character value.
 *
 * @property prefix The character value of the prefix
 */
internal enum class Numalgo2Prefix(val prefix: Char) {
    AUTHENTICATION('V'),
    KEY_AGREEMENT('E'),
    SERVICE('S')
}

/**
 * Prefix values used for encoding and decoding services.
 * Each prefix corresponds to a specific service type.
 */
private val ServicePrefix =
    mapOf(
        SERVICE_TYPE to "t",
        SERVICE_ENDPOINT to "s",
        SERVICE_DIDCOMM_MESSAGING to "dm",
        SERVICE_ROUTING_KEYS to "r",
        SERVICE_ACCEPT to "a",
        SERVICE_URI to "uri"
    )

/**
 * Encodes a service based on the provided JSON string.
 *
 * @param service The JSON string representing the service to encode.
 * @return The encoded service string.
 * @throws IllegalArgumentException If the JSON format is invalid.
 *
 * @see <a href="https://identity.foundation/peer-did-method-spec/#method-2-multiple-inception-key-without-doc">To encode a service</a>
 */
@Throws(IllegalArgumentException::class)
internal fun encodeService(service: JSON): String {
    validateJson(service)
    val trimmedService = service.trim()
    return when {
        trimmedService.startsWith("[") -> {
            /**
             * Process each service object individually if 'serviceEndpoint' is a JsonObject
             *
             */
            val jsonArray = Json.parseToJsonElement(trimmedService).jsonArray
            val firstElement = jsonArray.firstOrNull()?.jsonObject
            val isServiceEndpointObject = firstElement?.get("serviceEndpoint") is JsonObject

            if (isServiceEndpointObject) { // New Peer Did Spec
                jsonArray.joinToString(separator = "") { jsonElement ->
                    encodeIndividualService(jsonElement.toString())
                }
            } else {
                // Old approach combine service encoded
                encodeIndividualService(trimmedService)
            }
        }
        trimmedService.startsWith("{") -> {
            encodeIndividualService(trimmedService)
        }
        else -> throw IllegalArgumentException("Invalid JSON format")
    }
}

/**
 * Encodes an individual service object according to the second algorithm.
 *
 * @param service The service object to encode
 * @return The encoded service string
 */
fun encodeIndividualService(service: JSON): String {
    val serviceToEncode = service.replace(Regex("[\n\t\\s]*"), "")
        .replace(SERVICE_TYPE, ServicePrefix.getValue(SERVICE_TYPE))
        .replace(SERVICE_ENDPOINT, ServicePrefix.getValue(SERVICE_ENDPOINT))
        .replace(SERVICE_DIDCOMM_MESSAGING, ServicePrefix.getValue(SERVICE_DIDCOMM_MESSAGING))
        .replace(SERVICE_ROUTING_KEYS, ServicePrefix.getValue(SERVICE_ROUTING_KEYS))
        .replace(SERVICE_ACCEPT, ServicePrefix.getValue(SERVICE_ACCEPT))
    val encodedService = serviceToEncode.encodeToByteArray().base64UrlEncoded
    return ".${Numalgo2Prefix.SERVICE.prefix}$encodedService"
}

/**
 * Decodes [encodedServices] according to PeerDID spec
 * @see
 * <a href="https://identity.foundation/peer-did-method-spec/index.html#example-2-abnf-for-peer-dids">Specification</a>
 * @param [encodedServices] service to decode
 * @param [peerDID] PeerDID which will be used as an ID
 * @throws IllegalArgumentException if service is not correctly decoded
 * @return decoded service
 */
@Throws(IllegalArgumentException::class)
internal fun decodeService(encodedServices: List<JSON>, peerDID: PeerDID): List<Service>? {
    if (encodedServices.isEmpty()) {
        return null
    }
    val decodedServices = encodedServices.map { encodedService ->
        encodedService.base64PadDecoded
    }

    val decodedServicesJson = if (decodedServices.size == 1) {
        decodedServices[0]
    } else {
        decodedServices.joinToString(separator = ",", prefix = "[", postfix = "]")
    }

    val serviceMapList =
        try {
            fromJsonToList(decodedServicesJson)
        } catch (e: SerializationException) {
            try {
                listOf(fromJsonToMap(decodedServicesJson))
            } catch (e: SerializationException) {
                throw IllegalArgumentException("Invalid JSON $decodedServices")
            }
        }

    return serviceMapList.mapIndexed { serviceNumber, serviceMap ->
        if (!serviceMap.containsKey(ServicePrefix.getValue(SERVICE_TYPE))) {
            throw IllegalArgumentException("service doesn't contain a type")
        }

        val serviceType =
            serviceMap.getValue(ServicePrefix.getValue(SERVICE_TYPE)).toString()
                .replace(ServicePrefix.getValue(SERVICE_DIDCOMM_MESSAGING), SERVICE_DIDCOMM_MESSAGING)
        val serviceId = if (serviceMapList.size > 1) {
            if (serviceNumber == 0) {
                "#service"
            } else {
                "#service-$serviceNumber"
            }
        } else {
            "#service"
        }

        val serviceEndpointMap = mutableMapOf<String, Any>()
        when (val serviceEndpointValue = serviceMap[ServicePrefix.getValue(SERVICE_ENDPOINT)]) {
            is String -> {
                serviceMap[ServicePrefix.getValue(SERVICE_ENDPOINT)]?.let { serviceEndpointMap.put(SERVICE_URI, it) }
                serviceMap[ServicePrefix.getValue(SERVICE_ROUTING_KEYS)]?.let { serviceEndpointMap.put(SERVICE_ROUTING_KEYS, it) }
                serviceMap[ServicePrefix.getValue(SERVICE_ACCEPT)]?.let { serviceEndpointMap.put(SERVICE_ACCEPT, it) }
            }
            is Map<*, *> -> {
                serviceEndpointValue[ServicePrefix.getValue(SERVICE_URI)]?.let { serviceEndpointMap.put(SERVICE_URI, it) }
                serviceEndpointValue[ServicePrefix.getValue(SERVICE_ROUTING_KEYS)]?.let { serviceEndpointMap.put(SERVICE_ROUTING_KEYS, it) }
                serviceEndpointValue[ServicePrefix.getValue(SERVICE_ACCEPT)]?.let { serviceEndpointMap.put(SERVICE_ACCEPT, it) }
            }
            else -> {
                throw IllegalArgumentException("Service doesn't contain a valid Endpoint")
            }
        }

        val service =
            mutableMapOf<String, Any>(
                "id" to serviceId,
                "type" to serviceType,
                "serviceEndpoint" to serviceEndpointMap
            )

        OtherService(service)
    }.toList()
}

/**
 * Creates multibased encnumbasis according to PeerDID spec
 * @see
 * <a href="https://identity.foundation/peer-did-method-spec/index.html#method-specific-identifier">Specification</a>
 * @param [key] public key
 * @throws IllegalArgumentException if key is invalid
 * @return transform+encnumbasis
 */
internal fun createMultibaseEncnumbasis(key: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>): String {
    val decodedKey =
        when (key.format) {
            VerificationMaterialFormatPeerDID.BASE58 -> fromBase58(key.value.toString())
            VerificationMaterialFormatPeerDID.MULTIBASE -> fromMulticodec(fromBase58Multibase(key.value.toString()).second).second
            VerificationMaterialFormatPeerDID.JWK -> fromJwk(key)
        }
    validateRawKeyLength(decodedKey)
    return toBase58Multibase(toMulticodec(decodedKey, key.type))
}

/**
 * Represents a decoded encnumbasis as verification material.
 *
 * @property encnumbasis The encnumbasis value.
 * @property verMaterial The verification material.
 */
internal data class DecodedEncumbasis(
    val encnumbasis: String,
    val verMaterial: VerificationMaterialPeerDID<out VerificationMethodTypePeerDID>
)

/**
 * Decodes multibased encnumbasis to a verification material for DID DOC
 * @param [multibase] transform+encnumbasis to decode
 * @param [format] the format of public keys in the DID DOC
 * @throws IllegalArgumentException if key is invalid
 * @return decoded encnumbasis as verification material for DID DOC
 */
internal fun decodeMultibaseEncnumbasis(
    multibase: String,
    format: VerificationMaterialFormatPeerDID
): DecodedEncumbasis {
    val (encnumbasis, decodedEncnumbasis) = fromBase58Multibase(multibase)
    val (codec, decodedEncnumbasisWithoutPrefix) = fromMulticodec(decodedEncnumbasis)
    validateRawKeyLength(decodedEncnumbasisWithoutPrefix)

    val verMaterial =
        when (format) {
            VerificationMaterialFormatPeerDID.BASE58 ->
                when (codec) {
                    Codec.X25519 ->
                        VerificationMaterialAgreement(
                            format = format,
                            type = VerificationMethodTypeAgreement.X25519KeyAgreementKey2019,
                            value = toBase58(decodedEncnumbasisWithoutPrefix)
                        )
                    Codec.ED25519 ->
                        VerificationMaterialAuthentication(
                            format = format,
                            type = VerificationMethodTypeAuthentication.ED25519VerificationKey2018,
                            value = toBase58(decodedEncnumbasisWithoutPrefix)
                        )
                }
            VerificationMaterialFormatPeerDID.MULTIBASE ->
                when (codec) {
                    Codec.X25519 ->
                        VerificationMaterialAgreement(
                            format = format,
                            type = VerificationMethodTypeAgreement.X25519KeyAgreementKey2020,
                            value =
                            toBase58Multibase(
                                toMulticodec(
                                    decodedEncnumbasisWithoutPrefix,
                                    VerificationMethodTypeAgreement.X25519KeyAgreementKey2020
                                )
                            )
                        )
                    Codec.ED25519 ->
                        VerificationMaterialAuthentication(
                            format = format,
                            type = VerificationMethodTypeAuthentication.ED25519VerificationKey2020,
                            value =
                            toBase58Multibase(
                                toMulticodec(
                                    decodedEncnumbasisWithoutPrefix,
                                    VerificationMethodTypeAuthentication.ED25519VerificationKey2020
                                )
                            )
                        )
                }
            VerificationMaterialFormatPeerDID.JWK ->
                when (codec) {
                    Codec.X25519 ->
                        VerificationMaterialAgreement(
                            format = format,
                            type = VerificationMethodTypeAgreement.JsonWebKey2020,
                            value = toJwk(decodedEncnumbasisWithoutPrefix, VerificationMethodTypeAgreement.JsonWebKey2020)
                        )
                    Codec.ED25519 ->
                        VerificationMaterialAuthentication(
                            format = format,
                            type = VerificationMethodTypeAuthentication.JsonWebKey2020,
                            value =
                            toJwk(
                                decodedEncnumbasisWithoutPrefix,
                                VerificationMethodTypeAuthentication.JsonWebKey2020
                            )
                        )
                }
        }

    return DecodedEncumbasis(encnumbasis, verMaterial)
}

/**
 * Gets a verification method for a given DID and decoded encumbasis.
 *
 * @param keyId The ID of the key.
 * @param did The DID associated with the verification method.
 * @param decodedEncumbasis The decoded encumbasis object containing the encnumbasis and verification material.
 * @return The verification method.
 */
internal fun getVerificationMethod(keyId: Int, did: String, decodedEncumbasis: DecodedEncumbasis) =
    VerificationMethodPeerDID(
        id = "$did#key-$keyId",
        controller = did,
        verMaterial = decodedEncumbasis.verMaterial
    )
