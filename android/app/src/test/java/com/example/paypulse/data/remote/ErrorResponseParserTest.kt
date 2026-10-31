package com.example.paypulse.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

private const val VALIDATION_BODY = """
    {"code":"VALIDATION_ERROR","message":"One or more fields are invalid.",
     "fieldErrors":[{"field":"password","message":"Too short."},{"field":"confirmPassword","message":"No match."}]}
"""

class ErrorResponseParserTest {

    private val parser = ErrorResponseParser(createApiJson())

    private fun errorResponse(body: String): Response<Unit> =
        Response.error(400, body.toResponseBody("application/json".toMediaType()))

    @Test
    fun codeAndFieldNames_areRead() {
        val expected = ApiError("VALIDATION_ERROR", listOf("password", "confirmPassword"))
        assertEquals(expected, parser.apiError(errorResponse(VALIDATION_BODY)))
    }

    @Test
    fun missingFieldErrors_giveAnEmptyList() {
        val error = parser.apiError(errorResponse("""{"code":"ACTIVATION_TOKEN_INVALID","message":"x"}"""))
        assertEquals(ApiError("ACTIVATION_TOKEN_INVALID", emptyList()), error)
    }

    @Test
    fun httpException_isRead() {
        val error = parser.apiError(HttpException(errorResponse(VALIDATION_BODY)))
        assertEquals("VALIDATION_ERROR", error?.code)
    }

    @Test
    fun errorCode_returnsOnlyTheCode() {
        assertEquals("VALIDATION_ERROR", parser.errorCode(errorResponse(VALIDATION_BODY)))
    }

    @Test
    fun malformedOrEmptyBody_givesNull() {
        assertNull(parser.apiError(errorResponse("not json")))
        assertNull(parser.apiError(errorResponse("")))
        assertNull(parser.apiError(errorResponse("{}")))
    }
}
