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

private fun buildDIDDocNumalgo0(peerDID: PeerDID, format: VerificationMaterialFormatPeerDID): DIDDocPeerDID {
    val inceptionKey = peerDID.substring(10)
    val decodedEncumbasis = decodeMultibaseEncnumbasisAuth(inceptionKey, format)
    return DIDDocPeerDID(
        did = peerDID,
        authentication = listOf(getVerificationMethod(peerDID, decodedEncumbasis))
    )
}

private fun buildDIDDocNumalgo2(peerDID: PeerDID, format: VerificationMaterialFormatPeerDID): DIDDocPeerDID {
    val keys = peerDID.drop(11)

    var service = ""
    val authentications = mutableListOf<VerificationMethodPeerDID>()
    val keyAgreement = mutableListOf<VerificationMethodPeerDID>()

    keys.split(".").forEach {
        val prefix = it[0]
        val value = it.drop(1)

        when (prefix) {
            Numalgo2Prefix.SERVICE.prefix -> service = value

            Numalgo2Prefix.AUTHENTICATION.prefix -> {
                val decodedEncumbasis = decodeMultibaseEncnumbasisAuth(value, format)
                authentications.add(getVerificationMethod(peerDID, decodedEncumbasis))
            }

            Numalgo2Prefix.KEY_AGREEMENT.prefix -> {
                val decodedEncumbasis = decodeMultibaseEncnumbasisAgreement(value, format)
                keyAgreement.add(getVerificationMethod(peerDID, decodedEncumbasis))
            }

            else -> throw IllegalArgumentException("Unsupported transform part of PeerDID: $prefix")
        }
    }

    val decodedService = doDecodeService(service, peerDID)

    return DIDDocPeerDID(
        did = peerDID,
        authentication = authentications,
        keyAgreement = keyAgreement,
        service = decodedService
    )
}

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

private fun doDecodeService(service: String, peerDID: String): List<Service>? {
    try {
        return decodeService(service, peerDID)
    } catch (e: IllegalArgumentException) {
        throw MalformedPeerDIDException("Invalid service", e)
    }
}
