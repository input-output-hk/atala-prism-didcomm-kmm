@file:JvmName("PeerDIDResolver")

package io.iohk.atala.prism.didcomm.didpeer

import io.iohk.atala.prism.didcomm.didpeer.core.DecodedEncumbasis
import io.iohk.atala.prism.didcomm.didpeer.core.Numalgo2Prefix
import io.iohk.atala.prism.didcomm.didpeer.core.decodeMultibaseEncnumbasis
import io.iohk.atala.prism.didcomm.didpeer.core.decodeService
import io.iohk.atala.prism.didcomm.didpeer.core.getVerificationMethod
import io.iohk.atala.prism.didcomm.didpeer.core.validateAgreementMaterialType
import io.iohk.atala.prism.didcomm.didpeer.core.validateAuthenticationMaterialType
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

/** Resolves [DIDDocPeerDID] from [PeerDID]
 * @param [peerDID] PeerDID to resolve
 * @param [format] The format of public keys in the DID DOC. Default format is multibase.
 * @throws MalformedPeerDIDException
 * - if [peerDID] parameter does not match [peerDID] spec
 * - if a valid DIDDoc cannot be produced from the [peerDID]
 * @return resolved [DIDDocPeerDID] as JSON string
 */
@JvmOverloads
@Throws(IllegalArgumentException::class)
fun resolvePeerDID(
    peerDID: PeerDID,
    format: VerificationMaterialFormatPeerDID = VerificationMaterialFormatPeerDID.MULTIBASE
): String {
    if (!isPeerDID(peerDID)) {
        throw MalformedPeerDIDException("Does not match peer DID regexp: $peerDID")
    }
    val didDoc =
        when (peerDID[9]) {
            '0' -> buildDIDDocNumalgo0(peerDID, format)
            '2' -> buildDIDDocNumalgo2(peerDID, format)
            else -> throw IllegalArgumentException("Invalid numalgo of Peer DID: $peerDID")
        }
    return didDoc.toJson()
}

/**
 * Builds a PeerDID DID Document for the numalgo0 format.
 *
 * @param peerDID The PeerDID.
 * @param format The format of verification material in the DID Document.
 * @return The built DID Document.
 */
private fun buildDIDDocNumalgo0(peerDID: PeerDID, format: VerificationMaterialFormatPeerDID): DIDDocPeerDID {
    val inceptionKey = peerDID.substring(10)
    val decodedEncumbasis = decodeMultibaseEncnumbasisAuth(inceptionKey, format)
    return DIDDocPeerDID(
        did = peerDID,
        authentication = listOf(getVerificationMethod(1, peerDID, decodedEncumbasis))
    )
}

/**
 * Builds a PeerDID DID Document using the Numalgo2 algorithm.
 *
 * @param peerDID The PeerDID identifier.
 * @param format The format of the verification material.
 * @return The built PeerDID DID Document.
 * @throws IllegalArgumentException if the transform part of the PeerDID is unsupported.
 */
@Throws(IllegalArgumentException::class)
private fun buildDIDDocNumalgo2(peerDID: PeerDID, format: VerificationMaterialFormatPeerDID): DIDDocPeerDID {
    val keys = peerDID.drop(11)

    val encodedServicesJson = mutableListOf<JSON>()
    val authentications = mutableListOf<VerificationMethodPeerDID>()
    val keyAgreement = mutableListOf<VerificationMethodPeerDID>()

    keys.split(".").withIndex().forEach { (index, keyIt) ->
        val prefix = keyIt[0]
        val value = keyIt.drop(1)

        when (prefix) {
            Numalgo2Prefix.SERVICE.prefix -> {
                encodedServicesJson.add(value)
            }

            Numalgo2Prefix.AUTHENTICATION.prefix -> {
                val decodedEncumbasis = decodeMultibaseEncnumbasisAuth(value, format)
                authentications.add(getVerificationMethod(index + 1, peerDID, decodedEncumbasis))
            }

            Numalgo2Prefix.KEY_AGREEMENT.prefix -> {
                val decodedEncumbasis = decodeMultibaseEncnumbasisAgreement(value, format)
                keyAgreement.add(getVerificationMethod(index + 1, peerDID, decodedEncumbasis))
            }

            else -> throw IllegalArgumentException("Unsupported transform part of PeerDID: $prefix")
        }
    }

    val decodedService = doDecodeService(encodedServicesJson, peerDID)

    return DIDDocPeerDID(
        did = peerDID,
        authentication = authentications,
        keyAgreement = keyAgreement,
        service = decodedService
    )
}

/**
 * Decodes a multibase-encoded encnumbasis with a given verification material format.
 *
 * @param multibase The multibase-encoded encnumbasis to decode.
 * @param format The verification material format.
 * @throws MalformedPeerDIDException if the multibase is invalid.
 * @return The decoded encnumbasis as verification material.
 */
@Throws(MalformedPeerDIDException::class)
private fun decodeMultibaseEncnumbasisAuth(
    multibase: String,
    format: VerificationMaterialFormatPeerDID
): DecodedEncumbasis {
    try {
        val decodedEncumbasis = decodeMultibaseEncnumbasis(multibase, format)
        validateAuthenticationMaterialType(decodedEncumbasis.verMaterial)
        return decodedEncumbasis
    } catch (e: IllegalArgumentException) {
        throw MalformedPeerDIDException("Invalid key $multibase", e)
    }
}

/**
 * Decodes a multibase encoded number basis agreement to a verification material for DID DOC.
 *
 * @param multibase The multibase string to decode.
 * @param format The format of public keys in the DID DOC.
 * @throws MalformedPeerDIDException If the multibase string is invalid.
 * @return The decoded encnumbasis as verification material for DID DOC.
 */
@Throws(MalformedPeerDIDException::class)
private fun decodeMultibaseEncnumbasisAgreement(
    multibase: String,
    format: VerificationMaterialFormatPeerDID
): DecodedEncumbasis {
    try {
        val decodedEncumbasis = decodeMultibaseEncnumbasis(multibase, format)
        validateAgreementMaterialType(decodedEncumbasis.verMaterial)
        return decodedEncumbasis
    } catch (e: IllegalArgumentException) {
        throw MalformedPeerDIDException("Invalid key $multibase", e)
    }
}

/**
 * Decodes the provided list of service JSON objects according to the PeerDID spec.
 *
 * @param service The list of JSON objects representing services.
 * @param peerDID The PeerDID used as an ID.
 * @return The decoded list of services.
 * @throws MalformedPeerDIDException If the service is not correctly decoded.
 */
@Throws(MalformedPeerDIDException::class)
private fun doDecodeService(service: List<JSON>, peerDID: String): List<Service>? {
    try {
        return decodeService(service, peerDID)
    } catch (e: IllegalArgumentException) {
        throw MalformedPeerDIDException("Invalid service", e)
    }
}
