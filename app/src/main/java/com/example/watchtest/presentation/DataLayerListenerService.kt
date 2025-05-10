package com.example.watchtest.presentation

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

/**
 * Servicio que escucha mensajes entrantes desde el teléfono.
 * Este servicio funciona incluso cuando la app no está en primer plano.
 */
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

            // Guardar el mensaje recibido en SharedPreferences para acceso persistente
            getSharedPreferences("WearMessages", MODE_PRIVATE).edit()
                .putString("lastMessage", messageContent)
                .putString("senderNode", senderNodeId)
                .putLong("timestamp", System.currentTimeMillis())
                .apply()

            // Notificar a la actividad principal (si está en primer plano)
            // Esta acción se puede capturar con un BroadcastReceiver en MainActivity
            /*val intent = Intent(ACTION_MESSAGE_RECEIVED)
            intent.putExtra(EXTRA_MESSAGE, messageContent)
            intent.putExtra("senderNode", senderNodeId)
            sendBroadcast(intent)*/
        }
    }
}