package eu.codlab.push.livekit

data class InvitationPayload(
    val room: String,
    val inviter: String,
    val livekitUrl: String,
    val accessToken: String
)