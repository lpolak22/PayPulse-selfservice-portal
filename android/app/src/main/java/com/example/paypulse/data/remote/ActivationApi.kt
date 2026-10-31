package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.ActivateAccountRequestDto
import com.example.paypulse.data.remote.dto.ActivationStatusResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ActivationApi {
    @GET("api/auth/activation/{token}")
    suspend fun checkToken(@Path("token") token: String): ActivationStatusResponseDto

    @POST("api/auth/activation")
    suspend fun activate(@Body request: ActivateAccountRequestDto)
}
