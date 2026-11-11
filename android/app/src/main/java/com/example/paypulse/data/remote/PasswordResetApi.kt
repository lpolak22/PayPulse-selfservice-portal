package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.PasswordResetConfirmRequestDto
import com.example.paypulse.data.remote.dto.PasswordResetRequestDto
import com.example.paypulse.data.remote.dto.ResetTokenStatusResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PasswordResetApi {
    @POST("api/auth/password-reset/request")
    suspend fun requestReset(@Body request: PasswordResetRequestDto)

    @GET("api/auth/password-reset/{token}")
    suspend fun checkToken(@Path("token") token: String): ResetTokenStatusResponseDto

    @POST("api/auth/password-reset/confirm")
    suspend fun confirm(@Body request: PasswordResetConfirmRequestDto)
}
