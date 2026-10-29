package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.auth.AuthTokens
import com.example.paypulse.data.remote.dto.TokenResponseDto

fun TokenResponseDto.toAuthTokens(): AuthTokens = AuthTokens(accessToken, refreshToken)
