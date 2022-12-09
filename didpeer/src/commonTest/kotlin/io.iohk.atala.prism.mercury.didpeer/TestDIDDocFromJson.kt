package io.iohk.atala.prism.mercury.didpeer

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestDIDDocFromJson {

    data class TestData(
        val didDoc: JSON,
        val expectedFormat: VerificationMaterialFormatPeerDID,
        val expectedAuthType: VerificationMethodTypePeerDID,
        val expectedAgreemType: VerificationMethodTypePeerDID,
        val expectedField: PublicKeyField
    )

    @Test
    fun testDidDocFromJsonNumalgo0() {
        for (testData in didDocNumalgo0) {
            val didDoc = DIDDocPeerDID.fromJson(testData.didDoc)

            assertEquals(PEER_DID_NUMALGO_0, didDoc.did)

            assertTrue(didDoc.keyAgreement.isEmpty())
            assertNull(didDoc.service)
            assertEquals(1, didDoc.authentication.size)

            val auth = didDoc.authentication[0]
            val expectedAuth = (fromJson(testData.didDoc)["authentication"] as List<Map<String, Any>>)[0]
            assertEquals((expectedAuth["id"] as JsonPrimitive).content, auth.id)
            assertEquals(PEER_DID_NUMALGO_0, auth.controller)
            assertEquals(testData.expectedFormat, auth.verMaterial.format)
            assertEquals(testData.expectedAuthType, auth.verMaterial.type)

            if (testData.expectedField.value == PublicKeyField.JWK.value) {
                assertEquals(expectedAuth[testData.expectedField.value] as JsonObject, auth.verMaterial.value)
            } else {
                assertEquals((expectedAuth[testData.expectedField.value] as JsonPrimitive).content, auth.verMaterial.value)
            }

            assertEquals(listOf((expectedAuth["id"] as JsonPrimitive).content), didDoc.authenticationKids)
            assertTrue(didDoc.agreementKids.isEmpty())
        }
    }

    @Test
    fun testDidDocFromJsonNumalgo2() {
        for (testData in didDocNumalgo2) {
            val didDoc = DIDDocPeerDID.fromJson(testData.didDoc)

            assertEquals(PEER_DID_NUMALGO_2, didDoc.did)

            assertEquals(2, didDoc.authentication.size)
            assertEquals(1, didDoc.keyAgreement.size)
            assertNotNull(didDoc.service)
            assertEquals(1, didDoc.service?.size)

            val auth1 = didDoc.authentication[0]
            val expectedAuth1 = (fromJson(testData.didDoc)["authentication"] as List<Map<String, Any>>)[0]
            assertEquals((expectedAuth1["id"] as JsonPrimitive).content, auth1.id)
            assertEquals(PEER_DID_NUMALGO_2, auth1.controller)
            assertEquals(testData.expectedFormat, auth1.verMaterial.format)
            assertEquals(testData.expectedAuthType, auth1.verMaterial.type)

            if (testData.expectedField.value == PublicKeyField.JWK.value) {
                assertEquals(expectedAuth1[testData.expectedField.value] as JsonObject, auth1.verMaterial.value)
            } else {
                assertEquals((expectedAuth1[testData.expectedField.value] as JsonPrimitive).content, auth1.verMaterial.value)
            }

            val auth2 = didDoc.authentication[1]
            val expectedAuth2 = (fromJson(testData.didDoc)["authentication"] as List<Map<String, Any>>)[1]
            assertEquals((expectedAuth2["id"] as JsonPrimitive).content, auth2.id)
            assertEquals(PEER_DID_NUMALGO_2, auth2.controller)
            assertEquals(testData.expectedFormat, auth2.verMaterial.format)
            assertEquals(testData.expectedAuthType, auth2.verMaterial.type)

            if (testData.expectedField.value == PublicKeyField.JWK.value) {
                assertEquals((expectedAuth2[testData.expectedField.value] as JsonObject), auth2.verMaterial.value)
            } else {
                assertEquals((expectedAuth2[testData.expectedField.value] as JsonPrimitive).content, auth2.verMaterial.value)
            }

            val agreem = didDoc.keyAgreement[0]
            val expectedAgreem = (fromJson(testData.didDoc)["keyAgreement"] as List<Map<String, Any>>)[0]
            assertEquals((expectedAgreem["id"] as JsonPrimitive).content, agreem.id)
            assertEquals(PEER_DID_NUMALGO_2, agreem.controller)
            assertEquals(testData.expectedFormat, agreem.verMaterial.format)
            assertEquals(testData.expectedAgreemType.value, agreem.verMaterial.type.value)

            if (testData.expectedField.value == PublicKeyField.JWK.value) {
                assertEquals((expectedAgreem[testData.expectedField.value] as JsonObject), agreem.verMaterial.value)
            } else {
                assertEquals((expectedAgreem[testData.expectedField.value] as JsonPrimitive).content, agreem.verMaterial.value)
            }

            val service = didDoc.service!![0]
            val expectedService = (fromJson(testData.didDoc)["service"] as List<Map<String, Any>>)[0]
            assertTrue(service is DIDCommServicePeerDID)
            assertEquals((expectedService["id"] as JsonPrimitive).content, service.id)
            assertEquals((expectedService["serviceEndpoint"] as JsonPrimitive).content, service.serviceEndpoint)
            assertEquals((expectedService["type"] as JsonPrimitive).content, service.type)

            val expectedServiceRoutingKeys = (expectedService["routingKeys"] as JsonArray).map {
                it.jsonPrimitive.content
            }
            assertEquals(expectedServiceRoutingKeys, service.routingKeys)

            val expectedServiceAccept = (expectedService["accept"] as JsonArray).map {
                it.jsonPrimitive.content
            }
            assertEquals(expectedServiceAccept, service.accept)

            assertEquals(
                listOf(
                    (expectedAuth1["id"] as JsonPrimitive).content,
                    (expectedAuth2["id"] as JsonPrimitive).content
                ),
                didDoc.authenticationKids
            )

            assertEquals(listOf((expectedAgreem["id"] as JsonPrimitive).content), didDoc.agreementKids)
        }
    }

    @Test
    fun testDidDocFromJsonNumalgo2Service2Elements() {
        val didDoc = DIDDocPeerDID.fromJson(DID_DOC_NUMALGO_2_MULTIBASE_2_SERVICES)

        assertEquals(PEER_DID_NUMALGO_2_2_SERVICES, didDoc.did)

        assertNotNull(didDoc.service)
        assertEquals(2, didDoc.service?.size)

        val service1 = didDoc.service!![0]
        val expectedService1 =
            (fromJson(DID_DOC_NUMALGO_2_MULTIBASE_2_SERVICES)["service"] as List<Map<String, Any>>)[0]
        assertTrue(service1 is DIDCommServicePeerDID)
        assertEquals((expectedService1["id"] as JsonElement).jsonPrimitive.content, service1.id) // (expectedService1["id"] as JsonElement).jsonPrimitive.content
        assertEquals((expectedService1["serviceEndpoint"] as JsonElement).jsonPrimitive.content, service1.serviceEndpoint)
        assertEquals((expectedService1["type"] as JsonElement).jsonPrimitive.content, service1.type)
        val expectedService1RoutingKeys = (expectedService1["routingKeys"] as JsonArray).map {
            it.jsonPrimitive.content
        }
        assertEquals(expectedService1RoutingKeys, service1.routingKeys)
        assertTrue(service1.accept.isEmpty())

        val service2 = didDoc.service!![1]
        val expectedService2 =
            (fromJson(DID_DOC_NUMALGO_2_MULTIBASE_2_SERVICES)["service"] as List<Map<String, Any>>)[1]
        assertTrue(service2 is OtherService)
        assertEquals(expectedService2, service2.data)
    }

    @Test
    fun testDidDocFromJsonNumalgo2NoService() {
        val didDoc = DIDDocPeerDID.fromJson(DID_DOC_NUMALGO_2_MULTIBASE_NO_SERVICES)
        assertEquals(PEER_DID_NUMALGO_2_NO_SERVICES, didDoc.did)
        assertNull(didDoc.service)
        assertEquals(1, didDoc.authentication.size)
        assertEquals(1, didDoc.keyAgreement.size)
    }

    @Test
    fun testDidDocFromJsonNumalgo2MinimalService() {
        val didDoc = DIDDocPeerDID.fromJson(DID_DOC_NUMALGO_2_MULTIBASE_MINIMAL_SERVICES)
        assertEquals(PEER_DID_NUMALGO_2_MINIMAL_SERVICES, didDoc.did)

        assertEquals(2, didDoc.authentication.size)
        assertEquals(1, didDoc.keyAgreement.size)

        val service = didDoc.service!![0]
        assertTrue(service is DIDCommServicePeerDID)
        assertEquals(
            "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCJ9#didcommmessaging-0",
            service.id
        )
        assertEquals("https://example.com/endpoint", service.serviceEndpoint)
        assertEquals("DIDCommMessaging", service.type)
        assertTrue(service.routingKeys.isEmpty())
        assertTrue(service.accept.isEmpty())
    }

    @Test
    fun testDidDocFromJsonInvalidJson() {
        assertFails {
            DIDDocPeerDID.fromJson("sdfasdfsf{sdfsdfasdf...")
        }
    }

    @Test
    fun testDidDocIdFieldOnly() {
        val didDoc = DIDDocPeerDID.fromJson(
            """
   {
       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
   }
            """
        )
        assertEquals("did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V", didDoc.did)
    }

    @Test
    fun testDidDocInvalidJsonNoId() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "authentication": [
                           {
                               "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "type": "Ed25519VerificationKey2020",
                               "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "publicKeyMultibase": "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
                           }
                       ]
                   }
            """
            )
        }
    }

    @Test
    fun testDidDocInvalidJsonVerMethodNoId() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                       "authentication": [
                           {
                               "type": "Ed25519VerificationKey2020",
                               "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "publicKeyMultibase": "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
                           }
                       ]
                   }
            """
            )
        }
    }

    @Test
    fun testDidDocInvalidJsonVerMethodNoType() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                       "authentication": [
                           {
                               "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "publicKeyMultibase": "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
                           }
                       ]
                   }
            """
            )
        }
    }

    @Test
    fun testDidDocInvalidJsonVerMethodNoController() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                       "authentication": [
                           {
                               "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "type": "Ed25519VerificationKey2020",
                               "publicKeyMultibase": "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
                           }
                       ]
                   }
            """
            )
        }
    }

    @Test
    fun testDidDocInvalidJsonVerMethodNoValue() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                       "authentication": [
                           {
                               "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "type": "Ed25519VerificationKey2020",
                               "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
                           }
                       ]
                   }
            """
            )
        }
    }

    @Test
    fun testDidDocInvalidJsonVerMethodInvalidType() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                       "authentication": [
                           {
                               "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "type": "Unkknown",
                               "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "publicKeyMultibase": "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
                           }
                       ]
                   }
            """
            )
        }
    }

    @Test
    fun testDidDocInvalidJsonVerMethodInvalidField() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                       "authentication": [
                           {
                               "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "type": "Ed25519VerificationKey2020",
                               "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "publicKeyJwk": "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
                           }
                       ]
                   }
            """
            )
        }
    }

    @Test
    fun testDidDocInvalidJsonVerMethodInvalidValueJwk() {
        assertFails {
            DIDDocPeerDID.fromJson(
                """
                   {
                       "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                       "authentication": [
                           {
                               "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "type": "JsonWebKey2020",
                               "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
                               "publicKeyJwk": "sdfsdf{sfsdfdf"
                           }
                       ]
                   }
            """
            )
        }
    }

    companion object {

        val didDocNumalgo0: List<TestData> = listOf(
            TestData(
                DID_DOC_NUMALGO_O_BASE58,
                VerificationMaterialFormatPeerDID.BASE58,
                VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019,
                PublicKeyField.BASE58
            ),
            TestData(
                DID_DOC_NUMALGO_O_MULTIBASE,
                VerificationMaterialFormatPeerDID.MULTIBASE,
                VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020,
                PublicKeyField.MULTIBASE
            ),
            TestData(
                DID_DOC_NUMALGO_O_JWK,
                VerificationMaterialFormatPeerDID.JWK,
                VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                VerificationMethodTypeAgreement.JSON_WEB_KEY_2020,
                PublicKeyField.JWK
            )
        )

        val didDocNumalgo2: List<TestData> = listOf(
            TestData(
                DID_DOC_NUMALGO_2_BASE58,
                VerificationMaterialFormatPeerDID.BASE58,
                VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
                VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019,
                PublicKeyField.BASE58
            ),
            TestData(
                DID_DOC_NUMALGO_2_MULTIBASE,
                VerificationMaterialFormatPeerDID.MULTIBASE,
                VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2020,
                VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2020,
                PublicKeyField.MULTIBASE
            ),
            TestData(
                DID_DOC_NUMALGO_2_JWK,
                VerificationMaterialFormatPeerDID.JWK,
                VerificationMethodTypeAuthentication.JSON_WEB_KEY_2020,
                VerificationMethodTypeAgreement.JSON_WEB_KEY_2020,
                PublicKeyField.JWK
            )
        )
    }
}
