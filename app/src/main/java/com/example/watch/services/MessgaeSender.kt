package com.example.watch.services

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.nio.charset.StandardCharsets

object MessageSender {
    private const val TAG = "MessageSender"
    private const val MESSAGE_PATH = "/message"

    fun sendMessageToPhone(context: Context, message: String) {
        val messageClient: MessageClient = Wearable.getMessageClient(context)
        val nodeClient = Wearable.getNodeClient(context)
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val nodes = nodeClient.connectedNodes.await()
                for (node in nodes) {
                    messageClient.sendMessage(
                        node.id,
                        MESSAGE_PATH,
                        message.toByteArray(StandardCharsets.UTF_8)
                    ).addOnSuccessListener {
                        Log.d(TAG, "Mensaje enviado al nodo ${node.displayName}")
                    }.addOnFailureListener {
                        Log.e(TAG, "Error al enviar mensaje", it)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fallo al obtener nodos o enviar mensaje", e)
            }
        }
    }
}