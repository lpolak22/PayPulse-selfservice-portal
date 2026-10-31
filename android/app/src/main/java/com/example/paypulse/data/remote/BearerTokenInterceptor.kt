package com.example.paypulse.data.remote

import com.example.paypulse.data.auth.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class BearerTokenInterceptor(private val tokenStorage: TokenStorage) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking { tokenStorage.read() }?.accessToken
        val request = chain.request()
        return chain.proceed(if (accessToken == null) request else request.withBearerToken(accessToken))
    }
}
