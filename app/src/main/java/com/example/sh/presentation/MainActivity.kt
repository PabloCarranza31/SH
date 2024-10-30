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
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val LOCATION_REQUEST_CODE = 1000
    private var referenceLocation: Location? = null
    private val movementRadius = 1.0 // Radio en metros

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    Log.d("LocationUpdates", "Ubicación recibida: ${location.latitude}, ${location.longitude}")
                    checkLocationAndTriggerAlarm(location)
                }
            }

        }
    }

    private fun solicitarPermisosUbicacion() {
        Log.d("LocationUpdates", "Configurando ubicación de referencia...")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE)
        } else {
            setReferenceLocation()
            startLocationUpdates()
        }
    }

    private fun setReferenceLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                referenceLocation = location
                Log.d("LocationUpdates", "Ubicación de referencia establecida: ${referenceLocation?.latitude}, ${referenceLocation?.longitude}")
            } else {
                Toast.makeText(this, "Ubicación no disponible para referencia", Toast.LENGTH_SHORT).show()
            }
        }
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
                triggerAlarm()
            }
        }
    }

    private fun triggerAlarm() {
        // Vibrar el dispositivo
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))

        // Reproducir el sonido de la alarma
        val mediaPlayer = MediaPlayer.create(this, ) // Reemplaza con el nombre de tu archivo de sonido
        mediaPlayer.start()

        // Asegúrate de liberar los recursos después de que el sonido haya terminado
        mediaPlayer.setOnCompletionListener {
            it.release()
        }

        // Mostrar el mensaje de alerta
        Toast.makeText(this, "¡Fuera del radio permitido!", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setReferenceLocation()
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
