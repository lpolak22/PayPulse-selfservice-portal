package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.ErrorResponseDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response

class ErrorResponseParser(private val json: Json) {

    fun errorCode(exception: HttpException): String? = apiError(exception)?.code

    fun errorCode(response: Response<*>): String? = apiError(response)?.code

    fun apiError(exception: HttpException): ApiError? = exception.response()?.let { apiError(it) }

    fun apiError(response: Response<*>): ApiError? {
        val body = response.errorBody()?.string() ?: return null
        return try {
            json.decodeFromString<ErrorResponseDto>(body).toApiError()
        } catch (exception: SerializationException) {
            null
        }
    }

    private fun ErrorResponseDto.toApiError(): ApiError = ApiError(code, fieldErrors.map { it.field })
}
