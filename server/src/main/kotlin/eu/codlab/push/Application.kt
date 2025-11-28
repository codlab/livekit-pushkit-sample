package eu.codlab.push

import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.ApnsClientBuilder
import com.eatthepath.pushy.apns.auth.ApnsSigningKey
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
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
        module(apnsClients)
    }.start(wait = true)
}

fun Application.module(apnsClients: List<ApnsClient>) {
    routing {
        post("/token") {
            try {
                val body = call.receive<BodyToken>()
                call.respondText("body: ${body.token}")
            } catch (err: Throwable) {
                err.printStackTrace()
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

@Serializable
data class BodyToken(
    val token: String
)