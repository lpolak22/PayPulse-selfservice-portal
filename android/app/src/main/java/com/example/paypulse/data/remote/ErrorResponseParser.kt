package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.ErrorResponseDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response

class ErrorResponseParser(private val json: Json) {

    fun errorCode(exception: HttpException): String? = exception.response()?.let { errorCode(it) }

    fun errorCode(response: Response<*>): String? {
        val body = response.errorBody()?.string() ?: return null
        return try {
            json.decodeFromString<ErrorResponseDto>(body).code
        } catch (exception: SerializationException) {
            null
        }
    }
}
