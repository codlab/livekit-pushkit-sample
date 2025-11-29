package eu.codlab.push.livekit

import io.livekit.server.AccessToken
import io.livekit.server.RoomJoin
import io.livekit.server.RoomName
import io.livekit.server.RoomServiceClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomService internal constructor(
    private val url: String,
    private val urlWss: String,
    private val apiKey: String,
    private val apiSecret: String,
) {
    private val client = RoomServiceClient.createClient(
        host = url,
        apiKey = apiKey,
        secret = apiSecret
    )

    fun invite(room: String, inviter: String, invitee: String, deviceId: String) =
        InvitationPayload(
            room = room,
            inviter = inviter,
            livekitUrl = urlWss,
            accessToken = generateAccessToken(
                name = invitee,
                identity = deviceId,
                canJoinRoom = true,
                roomNames = arrayOf(room)
            )
        )

    private fun generateAccessToken(
        name: String,
        identity: String,
        canJoinRoom: Boolean,
        vararg roomNames: String
    ): String {
        val token = AccessToken(apiKey, apiSecret)
        token.name = name
        token.identity = identity
        token.metadata = "sample"
        token.addGrants(
            listOf(RoomJoin(canJoinRoom)) +
                    roomNames.map { RoomName(it) })

        return token.toJwt()
    }

    suspend fun createRoom(name: String) = client.createRoom(name).suspend()
}

suspend fun <T> Call<T>.suspend() = suspendCoroutine<T?> { continuation ->
    enqueue(object : Callback<T> {
        override fun onResponse(
            p0: Call<T?>,
            p1: Response<T?>
        ) {
            continuation.resume(p1.body())
        }

        override fun onFailure(
            p0: Call<T?>,
            p1: Throwable
        ) {
            continuation.resumeWithException(p1)
        }
    })
}
