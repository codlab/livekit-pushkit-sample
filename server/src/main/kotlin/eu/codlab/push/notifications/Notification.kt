package eu.codlab.push.notifications

import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.DeliveryPriority
import com.eatthepath.pushy.apns.PushType
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification.DEFAULT_EXPIRATION_PERIOD
import com.eatthepath.pushy.apns.util.TokenUtil
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Notification(
    private val token: String,
    private val appIdentifier: String,
    private val payload: String,
    private val voip: Boolean = false
) {
    val notification = SimpleApnsPushNotification(
        TokenUtil.sanitizeTokenString(token),
        if (voip) {
            "$appIdentifier.voip"
        } else {
            appIdentifier
        },
        payload,
        Instant.now().plus(DEFAULT_EXPIRATION_PERIOD),
        DeliveryPriority.IMMEDIATE,
        PushType.VOIP
    );

    suspend fun send(client: ApnsClient) = suspendCoroutine { continuation ->
        val result = client.sendNotification(notification)

        result.whenComplete { response, cause ->
            if (null != cause) {
                continuation.resumeWithException(cause)
            } else {
                continuation.resume(response)
            }
        }
    }
}