package eu.codlab.push

object ActualCallForwarder {
    private val callForwarder = CallForwarder()

    fun outgoingCall() = callForwarder.outgoingCall()

    fun incomingCall() = callForwarder.incomingCall()

    fun endCall() = callForwarder.endCall()
}

expect class CallForwarder() {
    fun outgoingCall()

    fun incomingCall()

    fun endCall()
}