/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watchtest.presentation

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : Activity(), MessageClient.OnMessageReceivedListener {

    private val TAG = "MainActivity"
    private lateinit var messageClient: MessageClient
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Listener registrado en onCreate")
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener(this)
    }

    override fun onResume() {
        super.onResume()
        messageClient.addListener(this)
        Log.d(TAG, "MessageClient listener registrado correctamente")
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                nodes.forEach {
                    Log.d(TAG, "Nodo conectado: ${it.id}, ${it.displayName}")
                }
            }
    }


    override fun onPause() {
        super.onPause()
        messageClient.removeListener(this)
        Log.d(TAG, "MessageClient listener unregistered")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "¡Recibido mensaje! PATH=${messageEvent.path}")
        if (messageEvent.path == "/message") {
            Log.d(TAG, "Mensaje recibido: iniciar medición de HR")
            // Aquí iría la lógica para iniciar la medición del sensor
        } else {
            Log.d(TAG, "Mensaje recibido en otra ruta: ${messageEvent.path}")
        }
    }
}


class MessageReceiverService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d("WearService", "Mensaje recibido con path: ${messageEvent.path}")
    }
}