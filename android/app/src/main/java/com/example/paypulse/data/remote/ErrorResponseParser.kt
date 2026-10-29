package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.ErrorResponseDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

class ErrorResponseParser(private val json: Json) {

    fun errorCode(exception: HttpException): String? {
        val body = exception.response()?.errorBody()?.string() ?: return null
        return try {
            json.decodeFromString<ErrorResponseDto>(body).code
        } catch (exception: SerializationException) {
            null
        }
    }
}
