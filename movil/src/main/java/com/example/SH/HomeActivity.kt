package com.example.SH

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.*

class HomeActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private lateinit var dataClient: DataClient
    private var selectedRadius: Int = 8 // Valor inicial del radio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        dataClient = Wearable.getDataClient(this)
        dataClient.addListener(this)

        val radiusSeekBar = findViewById<SeekBar>(R.id.radiusSeekBar)
        val radiusValue = findViewById<TextView>(R.id.radiusValue)
        val sendButton = findViewById<Button>(R.id.btnEnviar)

        // Seteo inicial del SeekBar
        radiusSeekBar.max = 200 // Ejemplo: máximo a 200 metros
        radiusSeekBar.progress = selectedRadius - 1 // Ajuste al índice del SeekBar
        radiusValue.text = "Radio: $selectedRadius m"

        // Listener del SeekBar
        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                selectedRadius = progress + 1 // Ajuste de valor mínimo a 1
                radiusValue.text = "Radio: $selectedRadius m"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Listener para el botón de enviar
        sendButton.setOnClickListener {
            sendRadiusToWearable(selectedRadius.toDouble())
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/location_update") {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val latitude = dataMapItem.dataMap.getDouble("latitude")
                val longitude = dataMapItem.dataMap.getDouble("longitude")
                Log.d("HomeActivity", "Ubicación recibida: lat=$latitude, long=$longitude")
                // Aquí puedes procesar la ubicación recibida
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataClient.removeListener(this)
    }

    private fun sendRadiusToWearable(radius: Double) {
        val dataMap = PutDataMapRequest.create("/radius_update").apply {
            dataMap.putDouble("radius", radius)
            Log.d("1", radius.toString())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        val putDataReq = dataMap.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }
}
