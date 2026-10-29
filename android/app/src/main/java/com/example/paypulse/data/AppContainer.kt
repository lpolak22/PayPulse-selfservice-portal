package com.example.paypulse.data

import com.example.paypulse.BuildConfig
import com.example.paypulse.data.auth.ApiAuthRepository
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.auth.AuthRepository
import kotlinx.serialization.json.Json
import retrofit2.Retrofit
import retrofit2.create

object AppContainer {
    private val json: Json by lazy { createApiJson() }
    private val retrofit: Retrofit by lazy { createRetrofit(BuildConfig.API_BASE_URL, json) }
    private val errorResponseParser: ErrorResponseParser by lazy { ErrorResponseParser(json) }

    val authRepository: AuthRepository by lazy {
        ApiAuthRepository(retrofit.create(), errorResponseParser)
    }
}
