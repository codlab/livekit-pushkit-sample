package eu.codlab.push

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.setActive
import platform.CallKit.CXAnswerCallAction
import platform.CallKit.CXCallController
import platform.CallKit.CXCallUpdate
import platform.CallKit.CXEndCallAction
import platform.CallKit.CXHandle
import platform.CallKit.CXProvider
import platform.CallKit.CXProviderConfiguration
import platform.CallKit.CXProviderDelegateProtocol
import platform.CallKit.CXStartCallAction
import platform.CallKit.CXTransaction
import platform.Foundation.NSUUID
import platform.darwin.NSObject
import kotlin.uuid.ExperimentalUuidApi

/**
 * https://developer.apple.com/documentation/callkit/cxhandle/handletype
 */
private enum class CallKitHandleType(val value: String, val swiftValue: Long) {
    generic("generic", 1),
    phoneNumber("phoneNumber", 2),
    emailAddress("emailAddress", 3)
}

private enum class KAVAudioSessionCategory(val swiftValue: String) {
    ambient("ambient"),
    multiRoute("multiRoute"),
    playAndRecord("playAndRecord"),
    playback("playback"),
    record("record"),
    soloAmbient("soloAmbien")
}

/**
 * https://developer.apple.com/documentation/avfaudio/avaudiosession/mode-swift.property
 */
private enum class KAVAudioSessionMode(val swiftValue: String) {
    default("default"),
    dualRoute("dualRoute"),
    gameChat("gameChat"),
    measurement("measurement"),
    moviePlayback("moviePlayback"),
    shortFormVideo("shortFormVideo"),
    spokenAudio("spokenAudio"),
    videoChat("videoChat"),
    voiceChat("voiceChat"),
    voicePrompt("voicePrompt")
}

/**
 * https://developer.apple.com/documentation/avfaudio/avaudiosession/categoryoptions-swift.struct
 */
private enum class KAVAudioSessionCategoryOptions(val swiftValue: Int) {
    allowAirPlay(0x40),
    allowBluetooth(0x4),
    allowBluetoothA2DP(0x20),

    // allowBluetoothHFP,
    // bluetoothHighQualityRecording,
    defaultToSpeaker(0x8),
    duckOthers(0x2),

    // farFieldInput,
    interruptSpokenAudioAndMixWithOthers(0x11),
    mixWithOthers(0x1),
    overrideMutedMicrophoneInterruption(0x80),
}

private var callManager: CallManager? = null

fun initializeCallManagerInstance(queue: NSObject) {
    callManager = CallManager(queue)
}

fun CallManagerInstance(): CallManager {
    if (null == callManager) throw IllegalStateException("call initializeCallManagerInstance(queue) first")

    return callManager!!
}

object CurrentCall {
    var livekitUrl: String? = null
        private set
    var currentName: String? = null
        private set

    var roomName: String? = null
        private set

    var liveKitToken: String? = null
        private set

    fun configure(payload: InvitationPayload) = configure(
        livekitUrl = payload.livekitUrl,
        currentName = payload.inviter,
        roomName = payload.room,
        liveKitToken = payload.accessToken
    )

    fun configure(
        livekitUrl: String,
        currentName: String,
        roomName: String,
        liveKitToken: String
    ) {
        this.livekitUrl = livekitUrl
        this.liveKitToken = liveKitToken

        this.currentName = currentName
        this.roomName = roomName
    }

    fun reset() {
        livekitUrl = null
        currentName = null
        liveKitToken = null
        roomName = null
    }
}

class CallManager(queue: NSObject) : NSObject(), CXProviderDelegateProtocol {
    private val provider: CXProvider
    private val callController = CXCallController()
    private var currentCallUuid: NSUUID? = null
    private var currentHandle: String? = null

    // infos livekit ?
    private var liveKitUrl: String? = null
    private var liveKitToken: String? = null

    init {
        var config = CXProviderConfiguration("MonApp VoIP")
        config.supportsVideo = true
        config.maximumCallGroups = 1U
        config.maximumCallsPerCallGroup = 1U
        config.supportedHandleTypes = setOf(CallKitHandleType.phoneNumber.swiftValue)

        provider = CXProvider(config)
        provider.setDelegate(this, queue)
    }

    override fun providerDidReset(provider: CXProvider) {
        println("providerDidReset")
        currentCallUuid = null
        currentHandle = null

        //TODO call to disconnect livekit
        stopAudioSessionIfNeeded()
    }

    override fun provider(provider: CXProvider, performStartCallAction: CXStartCallAction) {
        println("providerStartCallAction")
        startAudioSessionIfNeeded()

        connectLiveKit { success ->
            println("having success ? $success")
            if (success) {
                performStartCallAction.fulfill()
                provider.reportOutgoingCallWithUUID(
                    performStartCallAction.callUUID,
                    connectedAtDate = null
                )
            } else {
                performStartCallAction.fail()
            }
        }
    }

    override fun provider(provider: CXProvider, performAnswerCallAction: CXAnswerCallAction) {
        println("providerAnswerCallAction")
        startAudioSessionIfNeeded()

        connectLiveKit { success ->
            println("having success ? $success")
            if (success) {
                performAnswerCallAction.fulfill()
            } else {
                performAnswerCallAction.fail()
            }
        }
    }

    override fun provider(provider: CXProvider, performEndCallAction: CXEndCallAction) {
        println("providerEndCallAction")

        //TODO disconnect livekit

        stopAudioSessionIfNeeded()
        performEndCallAction.fulfill()
        currentCallUuid = null
        currentHandle = null
    }

    //TODO -> manage audio session with this dichotomy issue
    /*@OptIn(ExperimentalForeignApi::class)
    override fun provider(provider: CXProvider, didActivateAudioSession: AVAudioSession) {
        print("didActivateAudioSession AVAudioSession")
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun provider(provider: CXProvider, didDeactivateAudioSession: AVAudioSession) {
        super.provider(provider, didDeactivateAudioSession)
    }*/

    private fun connectLiveKit(completion: (Boolean) -> Unit) {
        completion(true)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun startAudioSessionIfNeeded() {
        val session = AVAudioSession.sharedInstance()
        try {
            session.setCategory(
                category = KAVAudioSessionCategory.playAndRecord.swiftValue,
                mode = KAVAudioSessionMode.voiceChat.swiftValue,
                options = setOf(
                    KAVAudioSessionCategoryOptions.allowBluetoothA2DP.swiftValue.toULong(),
                    KAVAudioSessionCategoryOptions.duckOthers.swiftValue.toULong()
                ).reduce { acc, l -> acc or l },
                error = null
            )
        } catch (err: Throwable) {
            println("unable to startAudioSessionIfNeeded $err")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun stopAudioSessionIfNeeded() {
        val session = AVAudioSession.sharedInstance()

        try {
            session.setActive(
                active = false,
                error = null
            )
        } catch (err: Throwable) {
            println("unable to stopAudioSessionIfNeeded $err")
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun startOutgoingCall(handle: String) {
        println("startOutgoingCall")
        val uuid = NSUUID()
        currentCallUuid = uuid
        currentHandle = handle

        val handleObject = CXHandle(CallKitHandleType.phoneNumber.swiftValue, handle)
        val startCallAction = CXStartCallAction(uuid, handleObject)
        val transaction = CXTransaction(startCallAction)

        request(transaction)
    }

    fun reportIncomingCall(handle: String) {
        println("reportIncomingCall")
        val uuid = NSUUID()
        currentCallUuid = uuid
        currentHandle = handle

        val update = CXCallUpdate()
        update.remoteHandle = CXHandle(CallKitHandleType.phoneNumber.swiftValue, handle)
        update.hasVideo = false
        update.localizedCallerName = "Appel de $handle"

        provider.reportNewIncomingCallWithUUID(uuid, update) { error ->
            if (null != error) {
                println("An error happened $error")
            } else {
                println("no error happened")
            }
        }
    }

    fun endCall() {
        println("endCall called")
        val uuid = currentCallUuid ?: return

        val endCallAction = CXEndCallAction(uuid)
        val transaction = CXTransaction(endCallAction)

        println("endCall request calling")
        request(transaction)
    }

    private fun request(transaction: CXTransaction) {
        println("request")
        callController.requestTransaction(transaction) { error ->
            if (null != error) {
                println("request for transaction $transaction error -> $error")
            } else {
                println("no error for transaction $transaction")
            }
        }
    }
}