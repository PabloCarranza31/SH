// Usuario.kt
package com.example.SH

import com.google.gson.annotations.SerializedName

data class Usuario(
    val _id: String,
    val nombre: String,
    val email: String
)

// Clase de datos para la solicitud de login
data class LoginRequest(
    val email: String,
    val password: String
)

data class User(
    @SerializedName("_id") val id: String, // Ajuste del nombre si el campo en JSON es "_id"
    val nombre: String,
    val email: String,
    val telefono: Long,
    val fecha_registro: String,
    val casco_id: String,
    val password: String
)

// Clase para la respuesta de login
data class LoginResponse(
    val token: String,
    val user: User // Anidamos el objeto `user`
)

