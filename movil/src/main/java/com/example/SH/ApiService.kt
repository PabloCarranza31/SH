// ApiService.kt
package com.example.SH

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("/api/users/")
    fun obtenerUsuarios(): Call<List<Usuario>>

    @POST("/api/users/login/")
    fun inicioSesion(
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @POST("usuarios")
    fun crearUsuario(@Body usuario: Usuario): Call<Usuario>
}
