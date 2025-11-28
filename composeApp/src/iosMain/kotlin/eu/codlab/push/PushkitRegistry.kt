package eu.codlab.push

import eu.codlab.push.push.PushControllerOnToken
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.base64Encoding
import platform.PushKit.PKPushCredentials
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
