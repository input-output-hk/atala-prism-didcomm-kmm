# DIDCommV2

[![Kotlin](https://img.shields.io/badge/kotlin-1.8.20-blue.svg?logo=kotlin)](http://kotlinlang.org)

![android](https://camo.githubusercontent.com/b1d9ad56ab51c4ad1417e9a5ad2a8fe63bcc4755e584ec7defef83755c23f923/687474703a2f2f696d672e736869656c64732e696f2f62616467652f706c6174666f726d2d616e64726f69642d3645444238442e7376673f7374796c653d666c6174)
![ios](https://camo.githubusercontent.com/1fec6f0d044c5e1d73656bfceed9a78fd4121b17e82a2705d2a47f6fd1f0e3e5/687474703a2f2f696d672e736869656c64732e696f2f62616467652f706c6174666f726d2d696f732d4344434443442e7376673f7374796c653d666c6174)
![jvm](https://camo.githubusercontent.com/700f5dcd442fd835875568c038ae5cd53518c80ae5a0cf12c7c5cf4743b5225b/687474703a2f2f696d672e736869656c64732e696f2f62616467652f706c6174666f726d2d6a766d2d4442343133442e7376673f7374796c653d666c6174)
![js](https://camo.githubusercontent.com/3e0a143e39915184b54b60a2ecedec75e801f396d34b5b366c94ec3604f7e6bd/687474703a2f2f696d672e736869656c64732e696f2f62616467652f706c6174666f726d2d6a732d4638444235442e7376673f7374796c653d666c6174)
![getNode-js](https://camo.githubusercontent.com/d08fda729ceebcae0f23c83499ca8f06105350f037661ac9a4cc7f58edfdbca9/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f706c6174666f726d2d6e6f64656a732d3638613036332e7376673f7374796c653d666c6174)

![Atala Prism Logo](../Logo.png)

Basic [DIDComm v2](https://identity.foundation/didcomm-messaging/spec) support in Kotlin Multiplatform.

This implementation is a re-implementation of this repo but for Kotlin Multiplatform

- [didcomm-jvm](https://github.com/sicpa-dlab/didcomm-jvm)

## Assumptions and Limitations
- Java 8+
- In order to use the library, `SecretResolver` and `DIDDocResolver` interfaces must be implemented on the application level.
  Implementation of that interfaces is out of DIDComm library scope.
    - Verification materials are expected in JWK, Base58 and Multibase formats.
        - In Base58 and Multibase formats, keys using only X25519 and Ed25519 curves are supported.
        - For private keys in Base58 and Multibase formats, the verification material value contains both private and public parts (concatenated bytes).
        - In Multibase format, bytes of the verification material value is prefixed with the corresponding Multicodec code.
    - Key IDs (kids) used in `SecretResolver` must match the corresponding key IDs from DID Doc verification methods.
    - Key IDs (kids) in DID Doc verification methods and secrets must be a full [DID Fragment](https://www.w3.org/TR/did-core/#fragment), that is `did#key-id`.
    - Verification methods referencing another DID Document are not supported (see [Referring to Verification Methods](https://www.w3.org/TR/did-core/#referring-to-verification-methods)).
- The following curves and algorithms are supported:
    - Encryption:
        - Curves: X25519, P-384, P-256, P-521
        - Content encryption algorithms:
            - XC20P (to be used with ECDH-ES only, default for anoncrypt),
            - A256GCM (to be used with ECDH-ES only),
            - A256CBC-HS512 (default for authcrypt)
        - Key wrapping algorithms: ECDH-ES+A256KW, ECDH-1PU+A256KW
    - Signing:
        - Curves: Ed25519, Secp256k1 (currently JDK < 15 only), P-256
        - Algorithms: EdDSA (with crv=Ed25519), ES256, ES256K (currently JDK < 15 only)
- DID rotation (`fromPrior` field) is supported.
- Forward protocol is implemented and used by default.
- Limitations and known issues:
    - Secp256k1 is supported on JDK < 15 only
- DIDComm has been implemented under the following [Assumptions](https://hackmd.io/i3gLqgHQR2ihVFV5euyhqg)

## Examples

See demo scripts for details:
- [DIDComm examples](lib/src/test/kotlin/org/didcommx/didcomm/DIDCommDemoTest.kt)
- [Routing examples](lib/src/test/kotlin/org/didcommx/didcomm/protocols/routing/DIDCommRoutingTest.kt)

A general usage of the API is the following:
- Sender Side:
    - Build a `Message` (plaintext, payload).
    - Convert a message to a DIDComm Message for further transporting by calling one of the following:
        - `packEncrypted` to build an Encrypted DIDComm message
        - `packSigned` to build a Signed DIDComm message
        - `packPlaintext` to build a Plaintext DIDComm message
- Receiver side:
    - Call `unpack` on receiver side that will decrypt the message, verify signature if needed
      and return a `Message` for further processing on the application level.

### 1. Build an Encrypted DIDComm message for the given recipient

This is the most common DIDComm message to be used in most of the applications.

A DIDComm encrypted message is an encrypted JWM (JSON Web Messages) that
- hides its content from all but authorized recipients
- (optionally) discloses and proves the sender to only those recipients
- provides message integrity guarantees

It is important in privacy-preserving routing. It is what normally moves over network transports in DIDComm
applications, and is the safest format for storing DIDComm data at rest.

See `packEncrypted` documentation for more details.

**Authentication encryption** example (most common case):
```kotlin
val didComm = DIDComm(DIDDocResolverMock(), SecretResolverMock())

// ALICE
val message = Message.builder(
    id = "1234567890",
    body = mapOf("messagespecificattribute" to "and its value"),
    type = "http://example.com/protocols/lets_do_lunch/1.0/proposal"
)
    .from(ALICE_DID)
    .to(listOf(BOB_DID))
    .createdTime(1516269022)
    .expiresTime(1516385931)
    .build()
val packResult = didComm.packEncrypted(
    PackEncryptedParams.builder(message, BOB_DID)
        .from(JWM.ALICE_DID)
        .build()
)
println("Sending ${packResult.packedMessage} to ${packResult.serviceMetadata?.serviceEndpoint ?: ""}")

// BOB
val unpackResult = didComm.unpack(
    UnpackParams.Builder(packResult.packedMessage).build()
)
println("Got ${unpackResult.message} message")
```

**Anonymous encryption** example:
```kotlin
val didComm = DIDComm(DIDDocResolverMock(), SecretResolverMock())
val message = Message.builder(
    id = "1234567890",
    body = mapOf("messagespecificattribute" to "and its value"),
    type = "http://example.com/protocols/lets_do_lunch/1.0/proposal"
)
    .to(listOf(BOB_DID))
    .createdTime(1516269022)
    .expiresTime(1516385931)
    .build()
val packResult = didComm.packEncrypted(
    PackEncryptedParams.builder(message, BOB_DID).build()
)
)
```

**Encryption with non-repudiation** example:
```kotlin
val didComm = DIDComm(DIDDocResolverMock(), SecretResolverMock())
val message = Message.builder(
    id = "1234567890",
    body = mapOf("messagespecificattribute" to "and its value"),
    type = "http://example.com/protocols/lets_do_lunch/1.0/proposal"
)
    .from(ALICE_DID)
    .to(listOf(BOB_DID))
    .createdTime(1516269022)
    .expiresTime(1516385931)
    .build()
val packResult = didComm.packEncrypted(
    PackEncryptedParams.builder(message, BOB_DID)
        .signFrom(ALICE_DID)
        .from(ALICE_DID)
        .build()
)
```

### 2. Build an unencrypted but Signed DIDComm message

Signed messages are only necessary when
- the origin of plaintext must be provable to third parties
- or the sender can’t be proven to the recipient by authenticated encryption because the recipient is not known in advance (e.g., in a
  broadcast scenario).

Adding a signature when one is not needed can degrade rather than enhance security because it
relinquishes the sender’s ability to speak off the record.

See `packSigned` documentation for more details.
```kotlin
val didComm = DIDComm(DIDDocResolverMock(), SecretResolverMock())

// ALICE
val message = Message.builder(
    id = "1234567890",
    body = mapOf("messagespecificattribute" to "and its value"),
    type = "http://example.com/protocols/lets_do_lunch/1.0/proposal"
)
    .from(ALICE_DID)
    .to(listOf(BOB_DID))
    .createdTime(1516269022)
    .expiresTime(1516385931)
    .build()
val packResult = didComm.packSigned(
    PackSignedParams.builder(message, ALICE_DID).build()
)
println("Publishing ${packResult.packedMessage}")

// BOB
val unpackResult = didComm.unpack(
    UnpackParams.Builder(packResult.packedMessage).build()
)
println("Got ${unpackResult.message} message")
```

### 3. Build a Plaintext DIDComm message

A DIDComm message in its plaintext form that
- is not packaged into any protective envelope
- lacks confidentiality and integrity guarantees
- repudiable

They are therefore not normally transported across security boundaries.
```kotlin
val didComm = DIDComm(DIDDocResolverMock(), SecretResolverMock())

// ALICE
val message = Message.builder(
    id = "1234567890",
    body = mapOf("messagespecificattribute" to "and its value"),
    type = "http://example.com/protocols/lets_do_lunch/1.0/proposal"
)
    .from(ALICE_DID)
    .to(listOf(BOB_DID))
    .createdTime(1516269022)
    .expiresTime(1516385931)
    .build()
val packResult = didComm.packPlaintext(
    PackPlaintextParams.builder(message)
        .build()
)
println("Publishing ${packResult.packedMessage}")

// BOB
val unpackResult = didComm.unpack(
    UnpackParams.Builder(packResult.packedMessage).build()
)
println("Got ${unpackResult.message} message")
```