package com.example.paypulse.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val CONNECT_TIMEOUT_SECONDS = 10L
private const val READ_TIMEOUT_SECONDS = 20L
private const val WRITE_TIMEOUT_SECONDS = 20L
private const val JSON_MEDIA_TYPE = "application/json"

fun createApiJson(): Json = Json { ignoreUnknownKeys = true }

fun createRetrofit(baseUrl: String, json: Json): Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(createOkHttpClient())
    .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
    .build()

private fun createOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    .build()
