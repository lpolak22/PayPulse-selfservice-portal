package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.LoginRequestDto
import com.example.paypulse.data.remote.dto.TokenResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenResponseDto
}
