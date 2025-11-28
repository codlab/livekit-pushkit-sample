package eu.codlab.push.push

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PushController {
    private var tokenFlow = MutableSharedFlow<String>(replay = 1)
    val tokens = tokenFlow.asSharedFlow()
    var lastToken: String? = null
        internal set

    internal fun onToken(token: String) {
        println("obtained voip token $token")
        lastToken = token
        tokenFlow.tryEmit(token)
    }
}