package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.AppUserDto
import retrofit2.http.GET

interface CurrentUserApi {
    @GET("api/auth/me")
    suspend fun me(): AppUserDto
}
