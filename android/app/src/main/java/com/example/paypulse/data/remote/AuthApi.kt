package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.LoginRequestDto
import com.example.paypulse.data.remote.dto.RefreshTokenRequestDto
import com.example.paypulse.data.remote.dto.TokenResponseDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): TokenResponseDto

    @POST("api/auth/refresh")
    fun refresh(@Body request: RefreshTokenRequestDto): Call<TokenResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequestDto): Response<Unit>
}
