package io.iohk.atala.prism.didcomm.didpeer

import kotlin.jvm.JvmOverloads

/**
 * The base class for all PeerDID errors and exceptions.
 *
 * @param message - the detail message.
 * @param cause - the cause of this.
 */
open class PeerDIDException @JvmOverloads constructor(message: String, cause: Throwable? = null) : Throwable(message, cause)

/**
 * Raised if the peer DID to be resolved in not a valid peer DID.
 *
 * @param message - the detail message.
 * @param cause - the cause of this.
 */
class MalformedPeerDIDException @JvmOverloads constructor(message: String, cause: Throwable? = null) :
    PeerDIDException("Invalid peer DID provided. $message", cause)

/**
 * Raised if the resolved peer DID Doc to be resolved in not a valid peer DID.
 *
 * @param cause - the cause of this.
 */
class MalformedPeerDIDDOcException @JvmOverloads constructor(cause: Throwable? = null) :
    PeerDIDException("Invalid peer DID Doc", cause)
