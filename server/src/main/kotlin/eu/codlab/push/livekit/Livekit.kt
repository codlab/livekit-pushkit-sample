package eu.codlab.push.livekit

object Livekit {
    fun create(url: String, urlWss: String, apiKey: String, apiSecret: String) = RoomService(
        url = url,
        urlWss = urlWss,
        apiKey = apiKey,
        apiSecret = apiSecret
    )
}
