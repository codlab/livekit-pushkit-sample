package eu.codlab.push

actual class CallForwarder {
    actual fun outgoingCall() {
        CallManagerInstance().startOutgoingCall("nothing")
    }

    actual fun incomingCall() {
        CallManagerInstance().reportIncomingCall("nothing")
    }

    actual fun endCall() {
        CallManagerInstance().endCall()
    }
}