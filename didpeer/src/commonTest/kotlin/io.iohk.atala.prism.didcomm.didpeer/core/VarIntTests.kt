package io.iohk.atala.prism.didcomm.didpeer.core

import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals

class VarIntTests {
    @Test
    fun testVarInt() {
        val origin = 1234774
        val byteBuffer = Buffer()
        VarInt.writeVarInt(origin, byteBuffer)
        val fin: Int = VarInt.readVarInt(byteBuffer)
        assertEquals(origin, fin)
    }

    @Test
    fun testVarIntX25519() {
        val origin = Codec.X25519.prefix
        val byteBuffer = Buffer()
        VarInt.writeVarInt(origin, byteBuffer)
        val fin: Int = VarInt.readVarInt(byteBuffer)
        assertEquals(origin, fin)
    }

    @Test
    fun testVarIntED25519() {
        val origin = Codec.ED25519.prefix
        val byteBuffer = Buffer()
        VarInt.writeVarInt(origin, byteBuffer)
        val fin: Int = VarInt.readVarInt(byteBuffer)
        assertEquals(origin, fin)
    }

//    @Test
//    fun test() {
//        val encryptionKeys = listOf(
//            VerificationMaterialAgreement(
//                type = VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019,
//                format = VerificationMaterialFormatPeerDID.BASE58,
//                value = "DmgBSHMqaZiYqwNMEJJuxWzsGGC8jUYADrfSdBrC6L8s",
//            )
//        )
//        val signingKeys = listOf(
//            VerificationMaterialAuthentication(
//                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
//                format = VerificationMaterialFormatPeerDID.BASE58,
//                value = "ByHnpUCFb1vAfh9CFZ8ZkmUZguURW8nSw889hy6rD8L7",
//            )
//        )
//        val signingKeys2 = listOf(
//            VerificationMaterialAuthentication(
//                type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
//                format = VerificationMaterialFormatPeerDID.MULTIBASE,
//                value = "zrv3hVor1kqRxzN51GT7Lt7C67km8v6TNStM8xx1wBzMjPgb9gCHyULbWTCM75Jf8Z4d7oH24z3D2bUwwNNx6FfLcLQ",
//            )
//        )
//        val service =
//            """
//        {
//            "type": "DIDCommMessaging",
//            "serviceEndpoint": "https://example.com/endpoint1",
//            "routingKeys": ["did:example:somemediator#somekey1"],
//            "accept": ["didcomm/v2", "didcomm/aip2;env=rfc587"]
//        }
//    """
//
//        val peerDIDAlgo0 = createPeerDIDNumalgo0(signingKeys[0])
//
//        val peerDIDAlgo02 = createPeerDIDNumalgo0(signingKeys2[0])
//
//        val didDocAlgo0Json = resolvePeerDID(peerDIDAlgo0)
//        val didDocAlgo0 = DIDDocPeerDID.fromJson(didDocAlgo0Json)
//
//        val peerDIDAlgo2 = createPeerDIDNumalgo2(
//            encryptionKeys, signingKeys, service
//        )
//        val didDocAlgo2Json = resolvePeerDID(peerDIDAlgo2)
//        val didDocAlgo2 = DIDDocPeerDID.fromJson(didDocAlgo2Json)
//    }
}
