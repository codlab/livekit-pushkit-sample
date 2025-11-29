package eu.codlab.push

import eu.codlab.push.push.PushControllerOnToken
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.serialization.json.Json
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.base64Encoding
import platform.PushKit.PKPushCredentials
import platform.PushKit.PKPushPayload
import platform.PushKit.PKPushRegistry
import platform.PushKit.PKPushRegistryDelegateProtocol
import platform.PushKit.PKPushType
import platform.darwin.NSObject
import platform.posix.memcpy

fun Instance(queue: NSObject, pushType: PKPushType): PushkitRegistry {
    if (null == _instance) {
        _instance = PushkitRegistry(queue, pushType)
        _instance!!.initialize()
    }

    return _instance!!
}

private var _instance: PushkitRegistry? = null

class PushkitRegistry(queue: NSObject, private val pushType: PKPushType) : NSObject(),
    PKPushRegistryDelegateProtocol {
    private val json = Json
    private val pkPushRegistry = PKPushRegistry(queue)

    fun initialize() {
        println("PushKitRegistry initialization")
        pkPushRegistry.delegate = this
        pkPushRegistry.desiredPushTypes = setOf(pushType)

        println("PushKitRegistry Initialized")
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun pushRegistry(
        registry: PKPushRegistry,
        didUpdatePushCredentials: PKPushCredentials,
        forType: PKPushType?
    ) {
        PushControllerOnToken(token = didUpdatePushCredentials.token.hexString())
    }

    /*override fun pushRegistry(
        registry: PKPushRegistry,
        didInvalidatePushTokenForType: PKPushType?
    ) {
        // TODO -> implement logic to invalidate it
    }*/

    override fun pushRegistry(
        registry: PKPushRegistry,
        didReceiveIncomingPushWithPayload: PKPushPayload,
        forType: PKPushType?
    ) {
        onIncomingPushWithPayload(didReceiveIncomingPushWithPayload, forType)
    }

    override fun pushRegistry(
        registry: PKPushRegistry,
        didReceiveIncomingPushWithPayload: PKPushPayload,
        forType: PKPushType?,
        withCompletionHandler: () -> Unit
    ) {
        onIncomingPushWithPayload(didReceiveIncomingPushWithPayload, forType, withCompletionHandler)
    }

    private fun onIncomingPushWithPayload(
        payload: PKPushPayload,
        forType: PKPushType?,
        completion: (() -> Unit)? = null
    ) {
        val map = payload.dictionaryPayload

        println("Notification :: $forType")
        map.forEach { (key, value) ->
            println("Notification :: key: $key, value: $value")
        }

        val payload = map["payload"]?.let { (it as NSString).toString() }?.let {
            json.decodeFromString(InvitationPayload.serializer(), it)
        }

        println("having payload $payload")

        CurrentCall.configure(payload!!)
        CallManagerInstance().reportIncomingCall(payload.inviter)

        completion?.invoke()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.byteArray() = ByteArray(length.toInt())
    .apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }

private fun NSData.hexString() = byteArray().asUByteArray()
    .joinToString("") { it.toString(radix = 16).padStart(2, '0') }

private fun NSData.base64() = this.base64Encoding()
