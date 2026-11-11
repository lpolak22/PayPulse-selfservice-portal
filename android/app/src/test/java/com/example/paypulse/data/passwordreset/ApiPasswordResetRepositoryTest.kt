package com.example.paypulse.data.passwordreset

import com.example.paypulse.data.activation.CannedResponseInterceptor
import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.passwordreset.PasswordResetError
import com.example.paypulse.domain.passwordreset.PasswordResetResult
import com.example.paypulse.domain.passwordreset.ResetTokenCheckResult
import com.example.paypulse.domain.passwordreset.ResetTokenStatus
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.create
import java.io.IOException

private const val TOKEN = "header.payload.signature"
private const val EMAIL = "ana@example.com"
private const val PASSWORD = "Lozinka1"

class ApiPasswordResetRepositoryTest {

    private val server = CannedResponseInterceptor()
    private val json = createApiJson()
    private val client = OkHttpClient.Builder().addInterceptor(server).build()
    private val repository = ApiPasswordResetRepository(
        createRetrofit("http://localhost/", json, client).create(),
        ErrorResponseParser(json)
    )

    private fun errorBody(code: String, vararg fields: String): String {
        val fieldErrors = fields.joinToString(",") { """{"field":"$it","message":"x"}""" }
        return """{"code":"$code","message":"x","fieldErrors":[$fieldErrors]}"""
    }

    private suspend fun resetFailure(httpCode: Int, body: String): PasswordResetResult {
        server.respond(httpCode, body)
        return repository.resetPassword(TOKEN, PASSWORD, PASSWORD)
    }

    @Test
    fun requestReset_postsTrimmedEmailAndSucceedsOnAccepted() = runTest {
        server.respond(202, """{"message":"If an account exists for this email, we've sent a reset link."}""")
        assertEquals(PasswordResetResult.Success, repository.requestReset("  $EMAIL "))
        val request = server.requests.single()
        assertEquals("POST", request.method)
        assertEquals("/api/auth/password-reset/request", request.path)
        assertEquals(buildJsonObject { put("email", JsonPrimitive(EMAIL)) }, json.parseToJsonElement(request.body.orEmpty()))
    }

    @Test
    fun requestReset_tooManyRequestsIsRateLimited() = runTest {
        server.respond(429, errorBody("PASSWORD_RESET_RATE_LIMITED"))
        assertEquals(PasswordResetResult.Failure(PasswordResetError.RATE_LIMITED), repository.requestReset(EMAIL))
    }

    @Test
    fun requestReset_invalidEmailIsMappedByField() = runTest {
        server.respond(400, errorBody("VALIDATION_ERROR", "email"))
        assertEquals(PasswordResetResult.Failure(PasswordResetError.INVALID_EMAIL), repository.requestReset("x"))
    }

    @Test
    fun checkToken_asksTheStatusEndpointForTheToken() = runTest {
        server.respond(200, """{"status":"VALID"}""")
        assertEquals(ResetTokenCheckResult.Checked(ResetTokenStatus.VALID), repository.checkToken(TOKEN))
        assertEquals("GET", server.requests.single().method)
        assertEquals("/api/auth/password-reset/$TOKEN", server.requests.single().path)
    }

    @Test
    fun expiredAndUsedStatuses_areMapped() = runTest {
        server.respond(200, """{"status":"EXPIRED"}""")
        assertEquals(ResetTokenCheckResult.Checked(ResetTokenStatus.EXPIRED), repository.checkToken(TOKEN))
        server.respond(200, """{"status":"USED"}""")
        assertEquals(ResetTokenCheckResult.Checked(ResetTokenStatus.USED), repository.checkToken(TOKEN))
    }

    @Test
    fun unknownStatusOrMalformedReply_isUnknownError() = runTest {
        server.respond(200, """{"status":"SOMETHING_NEW"}""")
        assertEquals(ResetTokenCheckResult.Failure(PasswordResetError.UNKNOWN), repository.checkToken(TOKEN))
        server.respond(200, "not json")
        assertEquals(ResetTokenCheckResult.Failure(PasswordResetError.UNKNOWN), repository.checkToken(TOKEN))
    }

    @Test
    fun invalidTokenOnCheck_isInvalidError() = runTest {
        server.respond(400, errorBody("PASSWORD_RESET_TOKEN_INVALID"))
        assertEquals(ResetTokenCheckResult.Failure(PasswordResetError.TOKEN_INVALID), repository.checkToken(TOKEN))
    }

    @Test
    fun offline_isNetworkErrorForAllCalls() = runTest {
        server.failure = IOException("offline")
        assertEquals(PasswordResetResult.Failure(PasswordResetError.NETWORK), repository.requestReset(EMAIL))
        assertEquals(ResetTokenCheckResult.Failure(PasswordResetError.NETWORK), repository.checkToken(TOKEN))
        assertEquals(PasswordResetResult.Failure(PasswordResetError.NETWORK), repository.resetPassword(TOKEN, PASSWORD, PASSWORD))
    }

    @Test
    fun resetPassword_postsTheValuesAndSucceedsOnNoContent() = runTest {
        server.respond(204)
        assertEquals(PasswordResetResult.Success, repository.resetPassword(TOKEN, PASSWORD, "$PASSWORD!"))
        val request = server.requests.single()
        val expectedBody = buildJsonObject {
            put("token", JsonPrimitive(TOKEN))
            put("password", JsonPrimitive(PASSWORD))
            put("confirmPassword", JsonPrimitive("$PASSWORD!"))
        }
        assertEquals("POST", request.method)
        assertEquals("/api/auth/password-reset/confirm", request.path)
        assertEquals(expectedBody, json.parseToJsonElement(request.body.orEmpty()))
    }

    @Test
    fun expiredOrUsedTokenOnReset_isMapped() = runTest {
        val expired = resetFailure(410, errorBody("PASSWORD_RESET_TOKEN_EXPIRED"))
        val used = resetFailure(410, errorBody("PASSWORD_RESET_TOKEN_USED"))
        assertEquals(PasswordResetResult.Failure(PasswordResetError.TOKEN_EXPIRED), expired)
        assertEquals(PasswordResetResult.Failure(PasswordResetError.TOKEN_USED), used)
    }

    @Test
    fun validationErrorsOnReset_mapByField() = runTest {
        val policy = resetFailure(400, errorBody("VALIDATION_ERROR", "password"))
        val mismatch = resetFailure(400, errorBody("VALIDATION_ERROR", "confirmPassword"))
        assertEquals(PasswordResetResult.Failure(PasswordResetError.PASSWORD_POLICY), policy)
        assertEquals(PasswordResetResult.Failure(PasswordResetError.PASSWORD_MISMATCH), mismatch)
    }

    @Test
    fun serverError_isUnknownError() = runTest {
        val result = resetFailure(500, errorBody("INTERNAL_ERROR"))
        assertEquals(PasswordResetResult.Failure(PasswordResetError.UNKNOWN), result)
    }
}
