package eu.codlab.push

import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.ApnsClientBuilder
import com.eatthepath.pushy.apns.auth.ApnsSigningKey
import eu.codlab.push.notifications.Notification
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File


fun main() {
    val appIdentifier = System.getenv("APNS_APP_IDENTIFIER")
    val teamId = System.getenv("APNS_TEAM_ID")
    val keyId = System.getenv("APNS_KEY_ID")
    val keyPath = System.getenv("APNS_KEY_PATH_P8")
    val apnsClients = listOf(
        ApnsClientBuilder.DEVELOPMENT_APNS_HOST,
        ApnsClientBuilder.PRODUCTION_APNS_HOST
    ).map {
        ApnsClientBuilder()
            .setApnsServer(it)
            .setSigningKey(
                ApnsSigningKey.loadFromPkcs8File(
                    File(keyPath),
                    teamId,
                    keyId
                )
            )
            // .setMetricsListener(MyCustomMetricsListener())
            .build()
    }

    embeddedServer(Netty, port = 4444, host = "127.0.0.1") {
        module(appIdentifier, apnsClients)
    }.start(wait = true)
}

fun Application.module(appIdentifier: String, apnsClients: List<ApnsClient>) {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }

    install(ContentNegotiation) {
        json(
            Json {
                explicitNulls = false
                prettyPrint = true
                isLenient = true
                encodeDefaults = true
            }
        )
    }

    val tokens = mutableListOf<BodyToken>()

    routing {
        post("/invite") {
            try {
                val body = call.receive<BodyInvite>()
                val deviceToken = tokens.firstOrNull { it.deviceId == body.deviceId }?.token
                    ?: throw IllegalArgumentException("device token not found")

                val notification = Notification(deviceToken, appIdentifier, true) {
                    // nothing for now
                }

                var result = ""

                apnsClients.forEachIndexed { index, client ->
                    result += "attempt to send to device via apns client $index...\n"
                    try {
                        notification.send(client)
                    } catch (err: Throwable) {
                        err.printStackTrace()
                        result += err.message
                    }
                }

                call.respondText(result)
            } catch (err: Throwable) {
                err.printStackTrace()
                call.respond(HttpStatusCode.NotFound)
            }
        }
        post("/token") {
            try {
                val body = call.receive<BodyToken>()
                tokens.removeIf { it.deviceId == body.deviceId }
                tokens.add(body)

                call.respondText("body: ${body.token}")
            } catch (err: Throwable) {
                err.printStackTrace()
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

@Serializable
data class BodyInvite(
    @SerialName("device_id")
    val deviceId: String,
    val room: String,
)

@Serializable
data class BodyToken(
    @SerialName("device_id")
    val deviceId: String,
    val token: String
)
