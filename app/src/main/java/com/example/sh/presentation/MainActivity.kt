package com.example.sh.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sh.R
import com.google.android.gms.location.*
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {



    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_REQUEST_CODE = 1000
    private var referenceLocation: Location? = null
    private val movementRadius = 8.0 // Radio en metros
    private var mediaPlayer: MediaPlayer? = null
    private var isAlarmPlaying = false
    private var isOutOfBounds = false // Verifica si el usuario está fuera del radio
    private lateinit var dataClient: DataClient

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("reloj",dataEvents.toString())
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/radius_update") {
                val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                val radius = dataMapItem.dataMap.getDouble("radius")
                Log.d("MainActivity", "Radio recibido: $radius metros")
                // Aquí puedes actualizar el radio en la aplicación del reloj
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataClient = Wearable.getDataClient(this)
        enableEdgeToEdge()

        // Inicializar el cliente de ubicación y el callback
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initializeLocationCallback()

        solicitarPermisosUbicacion()
    }

    private fun initializeLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    sendLocationToPhone(location.latitude, location.longitude)
                    Log.d("LocationUpdates", "Ubicación recibida: ${location.latitude}, ${location.longitude}")
                    checkLocationAndTriggerAlarm(location)
                }
            }

        }
    }

    fun sendLocationToPhone(latitude: Double, longitude: Double) {
        val dataMap = PutDataMapRequest.create("/location_update").apply {
            dataMap.putDouble("latitude", latitude)
            dataMap.putDouble("longitude", longitude)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        val putDataReq = dataMap.asPutDataRequest()
        dataClient.putDataItem(putDataReq)
    }

    private fun solicitarPermisosUbicacion() {
        Log.d("LocationUpdates", "Configurando ubicación de referencia...")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE)
        } else {
            setReferenceLocation(20.482020, -103.530463) // Ciudad de México
            startLocationUpdates()
        }
    }

    private fun setReferenceLocation(lat: Double, lon: Double) {
        // Crear una instancia de Location con las coordenadas proporcionadas
        val customLocation = Location("manual")
        customLocation.latitude = lat
        customLocation.longitude = lon

        // Asignar la ubicación personalizada como la ubicación de referencia
        referenceLocation = customLocation
        Log.d("LocationUpdates", "Ubicación de referencia manual establecida: ${referenceLocation?.latitude}, ${referenceLocation?.longitude}")
    }



    private fun startLocationUpdates() {
        // Crear un LocationRequest utilizando el nuevo constructor
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // Intervalo de 10 segundos
            .setWaitForAccurateLocation(true) // Opcional: espera a que se obtenga una ubicación más precisa
            .setMaxUpdates(Int.MAX_VALUE) // Opcional: limita a 1 actualización si deseas que solo obtenga la ubicación una vez
            .build()
        Log.d("actualizacion",locationRequest.toString())

        // Verifica los permisos antes de solicitar actualizaciones
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }





    private fun checkLocationAndTriggerAlarm(currentLocation: Location) {
        referenceLocation?.let { refLoc ->
            val distance = refLoc.distanceTo(currentLocation)
            if (distance > movementRadius) {
                if (!isOutOfBounds) {
                    isOutOfBounds = true
                    triggerAlarm() // Solo activa la alarma la primera vez que el usuario sale del radio
                }
            } else {
                // Si el usuario regresa dentro del radio, permite activar la alarma nuevamente al salir
                isOutOfBounds = false
                isAlarmPlaying = false
            }
        }
    }

    private fun triggerAlarm() {
        if (isAlarmPlaying) return // Evita que la alarma se active de nuevo si ya está sonando

        isAlarmPlaying = true

        // Vibrar el dispositivo
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))

        // Reproducir el sonido de la alarma
        mediaPlayer?.release() // Libera cualquier instancia anterior de MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.alarma).apply {
            isLooping = false // Asegura que no se reproduzca en bucle
            start()
            setOnCompletionListener {
                it.release()
                isAlarmPlaying = false // Permite que se pueda activar la alarma de nuevo cuando termine
            }
        }

        // Mostrar el mensaje de alerta
        Toast.makeText(this, "¡Fuera del radio permitido!", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setReferenceLocation(20.482020, -103.530463) // Ciudad de México
                    startLocationUpdates()
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }
}
