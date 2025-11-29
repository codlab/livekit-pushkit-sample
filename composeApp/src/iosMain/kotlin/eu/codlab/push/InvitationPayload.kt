package eu.codlab.push

import kotlinx.serialization.Serializable

@Serializable
data class InvitationPayload(
    val room: String,
    val inviter: String,
    val livekitUrl: String,
    val accessToken: String
)
