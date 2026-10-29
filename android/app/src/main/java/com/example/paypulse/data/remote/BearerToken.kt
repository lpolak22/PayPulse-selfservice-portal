package com.example.paypulse.data.remote

import okhttp3.Request

private const val AUTHORIZATION_HEADER = "Authorization"
private const val BEARER_PREFIX = "Bearer "

fun Request.withBearerToken(accessToken: String): Request = newBuilder()
    .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
    .build()

fun Request.bearerToken(): String? = header(AUTHORIZATION_HEADER)?.removePrefix(BEARER_PREFIX)
