package com.example.maternapp.services

import android.content.Intent
import android.util.Log
import com.example.maternapp.presentation.MainActivity
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
            //MessageSender.sendMessageToPhone(applicationContext, "Respondiendo chavales")

            val startIntent = Intent(this, MainActivity::class.java).apply {
                // FLAG_ACTIVITY_NEW_TASK es necesario porque estás iniciando
                // la Activity desde un contexto de Servicio.
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Opcional: puedes añadir más flags como FLAG_ACTIVITY_CLEAR_TOP
                // si quieres que solo haya una instancia de MainActivity.
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(startIntent)
        }
    }
}