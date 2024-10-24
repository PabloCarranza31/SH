package com.example.SH

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity(),CoroutineScope by MainScope(),
                DataClient.OnDataChangedListener,
                MessageClient.OnMessageReceivedListener,
                CapabilityClient.OnCapabilityChangedListener{

    lateinit var conectar: Button
    var activityContext: Context? = null

    private val wearableAppCheckPayload = "AppOpenWearable"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var wearableDeviceConnected:Boolean = false

    private var currentAckFromWearForAppOpenCheck: String? = null
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"
    private val MASSAGE_ITEM_RECEIVED_PATH = "/message-item-received"

    private val CHECK_MESSAGE = "hola"
    private var deviceConnected: Boolean =false
    private val PAYLOAD_PATH = "/APP_OPEN"
    lateinit var nodeID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        activityContext = this
        conectar=findViewById(R.id.button)

        conectar.setOnClickListener {
            if (!deviceConnected) {
                val tempAct: Activity = activityContext as MainActivity
                getNodes(tempAct) // Llama a getNodes para obtener el nodo

                // Verifica si ya se tiene un nodoID después de la llamada a getNodes
                if (::nodeID.isInitialized) {
                    // Enviar mensaje al nodo
                    val sendMessage = Wearable.getMessageClient(activityContext!!)
                        .sendMessage(nodeID, PAYLOAD_PATH, "Mensaje de prueba".toByteArray())
                        .addOnSuccessListener {
                            Log.d("MENSAJE", "Mensaje enviado con éxito.")
                        }
                        .addOnFailureListener { e ->
                            Log.d("PEDILLOS", "Error enviando el mensaje: ${e.message}")
                        }
                } else {
                    Log.d("ERROR", "No se encontró ningún nodo conectado.")
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            Wearable.getDataClient(activityContext!!).removeListener(this)
            Wearable.getMessageClient(activityContext!!).removeListener(this)
            Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("app://"),CapabilityClient.FILTER_REACHABLE)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getNodes(context: Context) {
        launch(Dispatchers.Default){
            val nodeList = Wearable.getNodeClient(context).connectedNodes
            try {
                val nodes = Tasks.await(nodeList)
                for (node in nodes){
                    Log.d("NODO",node.toString())
                    Log.d("NODO","El id de nodo es: ${node.id}")
                }
            }catch (exceptions: Exception){
                Log.d("Problemas", exceptions.toString())
            }
        }
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        TODO("Not yet implemented")
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }



    override fun onMessageReceived(ME: MessageEvent) {
        Log.d("onMessageReceived",ME.toString())
        Log.d("onMessageReceived","ID del nodo: ${ME.sourceNodeId}")
        Log.d("onMessageReceived","Payload: ${ME.path}")
        val message=String(ME.data,StandardCharsets.UTF_8)
        Log.d("onMessageReceived","Mensaje: ${message}")


    }
}