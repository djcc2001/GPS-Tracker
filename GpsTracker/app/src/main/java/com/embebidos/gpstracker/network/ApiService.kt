package com.embebidos.gpstracker.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

data class LocationRequest(
    val device_id: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val speed: Float,
    val battery: Int
)

data class LocationResponse(
    val message: String,
    val id: Int
)

interface ApiService {

    @POST("api/location")
    suspend fun sendLocation(
        @Header("x-api-key") apiKey: String,
        @Body location: LocationRequest
    ): LocationResponse

    @GET("api/locations/{device_id}")
    suspend fun getLocations(
        @Header("x-api-key") apiKey: String,
        @Path("device_id") deviceId: String
    ): List<LocationResponse>
}