package com.example.watch.services

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class DataLayerListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "MessageListener"
        const val MESSAGE_PATH = "/message"  // Debe coincidir con la app de Flutter
        const val ACTION_MESSAGE_RECEIVED = "com.example.wearapp.MESSAGE_RECEIVED"
        const val EXTRA_MESSAGE = "message"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Mensaje recibido en servicio: ${messageEvent.path}")

        // Verificar si es nuestro mensaje
        if (messageEvent.path == MESSAGE_PATH) {
            val messageContent = String(messageEvent.data)
            val senderNodeId = messageEvent.sourceNodeId

            Log.d(TAG, "Contenido del mensaje: $messageContent")
            Log.d(TAG, "Enviado por nodo: $senderNodeId")
            MessageSender.sendMessageToPhone(applicationContext, "Respondiendo chavales")

        }
    }
}