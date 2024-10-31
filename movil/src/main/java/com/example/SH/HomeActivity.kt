package com.example.SH

import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.*

class HomeActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private lateinit var dataClient: DataClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        dataClient = Wearable.getDataClient(this)
        dataClient.addListener(this)

        val radiusSeekBar = findViewById<SeekBar>(R.id.radiusSeekBar)
        val radiusValue = findViewById<TextView>(R.id.radiusValue)

        // Ajuste inicial del SeekBar
        radiusSeekBar.max = 200 // Ejemplo: máximo a 50 metros
        radiusSeekBar.progress = 7 // Ejemplo: valor mínimo inicial a 8 metros
        radiusValue.text = "Radio: ${radiusSeekBar.progress + 1} m"

        // Listener del SeekBar
        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val selectedRadius = progress + 1 // Ajuste de valor mínimo a 1
                radiusValue.text = "Radio: $selectedRadius m"
                sendRadiusToWearable(selectedRadius.toDouble())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
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
            Log.d("1",radius.toString())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        val putDataReq = dataMap.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }
}
