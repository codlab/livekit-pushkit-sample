package eu.codlab.push

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vopenia.livekit.Room
import eu.codlab.http.createClient
import eu.codlab.push.push.PushController
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import push.composeapp.generated.resources.Res
import push.composeapp.generated.resources.compose_multiplatform

@Serializable
data class BodyToken(
    val token: String,
    val device_id: String
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        val token by PushController.tokens.collectAsState("")

        LaunchedEffect(Unit) {
            val client = createClient()

            async {
                PushController.tokens.collect {
                    println("having token -> $it")
                    val answer = client.post("https://0ee15b118e6b.ngrok-free.app/token") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            BodyToken(
                                token = it,
                                device_id = "test"
                            )
                        )
                    }
                    println("having answer ${answer.status} ${answer.bodyAsText()}")
                }
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = {
                    showContent = !showContent
                    //ActualCallForwarder.outgoingCall()

                    GlobalScope.launch {
                        val room = Room()

                        room.connect(
                            "wss://vopenia-5m9onn6o.livekit.cloud",
                            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUElHUVRlaXZHVEJKZXQiLCJleHAiOjE3NjQ0NjczNTMsInN1YiI6InRlc3QiLCJuYW1lIjoidGVzdCIsIm1ldGFkYXRhIjoic2FtcGxlIiwidmlkZW8iOnsicm9vbUpvaW4iOnRydWUsInJvb20iOiJ3ZWIifSwic2lwIjp7fX0.RLJRvL2QHzFa3JeYs6z864JrJmxNqAhX1UtiAnuNb_A"
                        )
                    }
                }
            ) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Token: $token")
                }
            }
        }
    }
}