package com.example.SH

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var usuarioAdapter: UsuarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword) // Campo de contraseña
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etCorreo.text.toString()
            val password = etPassword.text.toString()
            iniciarSesion(email, password)
        }
    }

    private fun iniciarSesion(email: String, password: String) {
        val loginRequest = LoginRequest(email, password) // Ajusta con tus datos de solicitud
        val call = RetrofitClient.apiService.inicioSesion(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    val userId = loginResponse?.user?.id
                    Log.d("UserId", "ID del usuario: $userId")

                    // Iniciar la nueva actividad
                    val intent = Intent(this@MainActivity, HomeActivity::class.java)
                    intent.putExtra("USER_ID", userId) // Opcional: pasar el ID del usuario a la nueva actividad
                    startActivity(intent)

                    // Opcional: Finalizar la actividad actual si no quieres volver a ella con el botón de retroceso
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Error en login", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                Log.e("API_ERROR", "Fallo en la conexión: ${t.message}")
            }
        })
    }

    private fun obtenerUsuarios() {
        val call = RetrofitClient.apiService.obtenerUsuarios()

        call.enqueue(object : Callback<List<Usuario>> {
            override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                if (response.isSuccessful) {
                    val usuarios = response.body() ?: emptyList()
                    usuarioAdapter = UsuarioAdapter(usuarios)
                    recyclerView.adapter = usuarioAdapter
                } else {
                    Log.e("API_ERROR", "Error en la respuesta: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                Log.e("API_ERROR", "Fallo en la conexión: ${t.message}")
            }
        })
    }
}
