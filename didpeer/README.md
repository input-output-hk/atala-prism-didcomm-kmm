# PeerDID

[![Kotlin](https://img.shields.io/badge/kotlin-1.9.22-blue.svg?logo=kotlin)](http://kotlinlang.org)
![badge-license]
![badge-latest-release]
[![semantic-release-kotlin]](https://github.com/semantic-release/semantic-release)

![badge-platform-android]
![badge-platform-ios]
![badge-platform-jvm]
![badge-platform-js]
![badge-platform-js-node]

![Atala Prism Logo](../Logo.png)

Implementation of the [Peer DID method specification](https://identity.foundation/peer-did-method-spec/) in Kotlin MultiPlatform with support for the following targets:

- JS
- iOS
- Android
- JVM

This implementation is a re-implementation of this repo but for Kotlin Multiplatform

- [peer-did-jvm](https://github.com/sicpa-dlab/peer-did-jvm)

## How to use for another KMP (Kotlin Multiplatform) project

### Using Groovy

In the project `build.gradle`
```groovy
allprojects {
    repositories {
        // along with all the other current existing repos add the following
        mavenCentral()
    }
}
```
In the module `build.gradle`
```groovy
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // This following is just an example you can import it as per you needs
                implementation 'io.iohk.atala.prism.didcomm:didpeer:<latest version>'
            }
        }
    }
}
```

### Using Kotlin DSL

In the project `build.gradle.kts`
```kotlin
allprojects {
    repositories {
        // along with all the other current existing repos add the following
        mavenCentral()
    }
}
```
```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // This following is just an example you can import it as per you needs
                implementation("io.iohk.atala.prism.didcomm:didpeer:<latest version>")
            }
        }
    }
}
```

## How to use for Scala Project

```scala
libraryDependencies += "io.iohk.atala.prism.didcomm" % "didpeer-jvm" % "<latest version>"
```

## Example

Example code:

```kotlin
val encryptionKeys = listOf(
    VerificationMaterialAgreement(
        type = VerificationMethodTypeAgreement.X25519_KEY_AGREEMENT_KEY_2019,
        format = VerificationMaterialFormatPeerDID.BASE58,
        value = "DmgBSHMqaZiYqwNMEJJuxWzsGGC8jUYADrfSdBrC6L8s",
    )
)
val signingKeys = listOf(
    VerificationMaterialAuthentication(
        type = VerificationMethodTypeAuthentication.ED25519_VERIFICATION_KEY_2018,
        format = VerificationMaterialFormatPeerDID.BASE58,
        value = "ByHnpUCFb1vAfh9CFZ8ZkmUZguURW8nSw889hy6rD8L7",
    )
)
val service =
    """
        {
            "type": "DIDCommMessaging",
            "serviceEndpoint": "https://example.com/endpoint1",
            "routingKeys": ["did:example:somemediator#somekey1"],
            "accept": ["didcomm/v2", "didcomm/aip2;env=rfc587"]
        }
    """

val peerDIDAlgo0 = createPeerDIDNumalgo0(signingKeys[0])
val peerDIDAlgo2 = createPeerDIDNumalgo2(
    encryptionKeys, signingKeys, service
)

val didDocAlgo0Json = resolvePeerDID(peerDIDAlgo0)
val didDocAlgo2Json = resolvePeerDID(peerDIDAlgo2)

val didDocAlgo0 = DIDDocPeerDID.fromJson(didDocAlgo0Json)
val didDocAlgo2 = DIDDocPeerDID.fromJson(didDocAlgo2Json)
```

Example of DID documents:

### DIDDoc algo 0:
```json
{
    "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
    "authentication": [
        {
            "id": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
            "type": "Ed25519VerificationKey2020",
            "controller": "did:peer:0z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
            "publicKeyMultibase": "ByHnpUCFb1vAfh9CFZ8ZkmUZguURW8nSw889hy6rD8L7"
        }
    ]
}
```
### did_doc_algo_2
```json
{
   "id": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0",
   "authentication": [
       {
           "id": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0#6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V",
           "type": "Ed25519VerificationKey2020",
           "controller": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0",
           "publicKeyMultibase": "z6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V"
       },
       {
           "id": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0#6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg",
           "type": "Ed25519VerificationKey2020",
           "controller": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0",
           "publicKeyMultibase": "z6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg"
       }
   ],
   "keyAgreement": [
       {
           "id": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0#6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc",
           "type": "X25519KeyAgreementKey2020",
           "controller": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0",
           "publicKeyMultibase": "z6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc"
       }
   ],
   "service": [
       {
           "id": "did:peer:2.Ez6LSbysY2xFMRpGMhb7tFTLMpeuPRaqaWM1yECx2AtzE3KCc.Vz6MkqRYqQiSgvZQdnBytw86Qbs2ZWUkGv22od935YF4s8M7V.Vz6MkgoLTnTypo3tDRwCkZXSccTPHRLhF4ZnjhueYAFpEX6vg.SeyJ0IjoiZG0iLCJzIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9lbmRwb2ludCIsInIiOlsiZGlkOmV4YW1wbGU6c29tZW1lZGlhdG9yI3NvbWVrZXkiXSwiYSI6WyJkaWRjb21tL3YyIiwiZGlkY29tbS9haXAyO2Vudj1yZmM1ODciXX0#didcommmessaging-0",
           "type": "DIDCommMessaging",
           "serviceEndpoint": "https://example.com/endpoint",
           "routingKeys": [
               "did:example:somemediator#somekey"
           ],
           "accept": [
                "didcomm/v2", "didcomm/aip2;env=rfc587"
           ]
       }
   ]
}
```

## Assumptions and limitations
- Only static layers [1, 2a, 2b](https://identity.foundation/peer-did-method-spec/#layers-of-support) are supported
- Only `X25519` keys are support for key agreement
- Only `Ed25519` keys are support for authentication
- Supported verification materials (input and in the resolved DID DOC):
    - [Default] 2020 verification materials (`Ed25519VerificationKey2020` and `X25519KeyAgreementKey2020`) with multibase base58 (`publicKeyMultibase`) public key encoding.
    - JWK (`JsonWebKey2020`) using JWK (`publicKeyJwk`) public key encoding
    - 2018/2019 verification materials (`Ed25519VerificationKey2018` and `X25519KeyAgreementKey2019`) using base58 (`publicKeyBase58`) public key encoding.

<!-- TAG_VERSION -->
[badge-latest-release]: https://img.shields.io/badge/latest--release-1.0.0-blue.svg?style=flat
[badge-license]: https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat
[semantic-release-kotlin]: https://img.shields.io/badge/semantic--release-kotlin-blue?logo=semantic-release

<!-- TAG_PLATFORMS -->
[badge-platform-android]: http://img.shields.io/badge/-android-6EDB8D.svg?style=flat
[badge-platform-ios]: http://img.shields.io/badge/-ios-CDCDCD.svg?style=flat
[badge-platform-jvm]: http://img.shields.io/badge/-jvm-DB413D.svg?style=flat
[badge-platform-js]: http://img.shields.io/badge/-js-F8DB5D.svg?style=flat
[badge-platform-js-node]: https://img.shields.io/badge/-nodejs-68a063.svg?style=flat
