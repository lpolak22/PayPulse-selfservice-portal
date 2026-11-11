package com.example.paypulse.data.activation

import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.activation.ActivationError
import com.example.paypulse.domain.activation.ActivationResult
import com.example.paypulse.domain.activation.ActivationTokenStatus
import com.example.paypulse.domain.activation.TokenCheckResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.create
import java.io.IOException

private const val TOKEN = "header.payload.signature"
private const val PASSWORD = "Lozinka1"

class ApiActivationRepositoryTest {

    private val server = CannedResponseInterceptor()
    private val json = createApiJson()
    private val client = OkHttpClient.Builder().addInterceptor(server).build()
    private val repository = ApiActivationRepository(
        createRetrofit("http://localhost/", json, client).create(),
        ErrorResponseParser(json)
    )

    private fun errorBody(code: String, vararg fields: String): String {
        val fieldErrors = fields.joinToString(",") { """{"field":"$it","message":"x"}""" }
        return """{"code":"$code","message":"x","fieldErrors":[$fieldErrors]}"""
    }

    private suspend fun activationFailure(httpCode: Int, body: String): ActivationResult {
        server.respond(httpCode, body)
        return repository.activate(TOKEN, PASSWORD, PASSWORD)
    }

    @Test
    fun checkToken_asksTheStatusEndpointForTheToken() = runTest {
        server.respond(200, """{"status":"VALID"}""")
        assertEquals(TokenCheckResult.Checked(ActivationTokenStatus.VALID), repository.checkToken(TOKEN))
        assertEquals("GET", server.requests.single().method)
        assertEquals("/api/auth/activation/$TOKEN", server.requests.single().path)
    }

    @Test
    fun expiredAndUsedStatuses_areMapped() = runTest {
        server.respond(200, """{"status":"EXPIRED"}""")
        assertEquals(TokenCheckResult.Checked(ActivationTokenStatus.EXPIRED), repository.checkToken(TOKEN))
        server.respond(200, """{"status":"USED"}""")
        assertEquals(TokenCheckResult.Checked(ActivationTokenStatus.USED), repository.checkToken(TOKEN))
    }

    @Test
    fun unknownStatusOrMalformedReply_isUnknownError() = runTest {
        server.respond(200, """{"status":"SOMETHING_NEW"}""")
        assertEquals(TokenCheckResult.Failure(ActivationError.UNKNOWN), repository.checkToken(TOKEN))
        server.respond(200, "not json")
        assertEquals(TokenCheckResult.Failure(ActivationError.UNKNOWN), repository.checkToken(TOKEN))
    }

    @Test
    fun invalidTokenOnCheck_isInvalidError() = runTest {
        server.respond(400, errorBody("ACTIVATION_TOKEN_INVALID"))
        assertEquals(TokenCheckResult.Failure(ActivationError.TOKEN_INVALID), repository.checkToken(TOKEN))
    }

    @Test
    fun serverErrorOnCheck_isUnknownError() = runTest {
        server.respond(500, errorBody("INTERNAL_ERROR"))
        assertEquals(TokenCheckResult.Failure(ActivationError.UNKNOWN), repository.checkToken(TOKEN))
    }

    @Test
    fun offline_isNetworkErrorForBothCalls() = runTest {
        server.failure = IOException("offline")
        assertEquals(TokenCheckResult.Failure(ActivationError.NETWORK), repository.checkToken(TOKEN))
        assertEquals(ActivationResult.Failure(ActivationError.NETWORK), repository.activate(TOKEN, PASSWORD, PASSWORD))
    }

    @Test
    fun activate_postsTheValuesAndSucceedsOnNoContent() = runTest {
        server.respond(204)
        assertEquals(ActivationResult.Success, repository.activate(TOKEN, PASSWORD, "$PASSWORD!"))
        val request = server.requests.single()
        val expectedBody = buildJsonObject {
            put("token", JsonPrimitive(TOKEN))
            put("password", JsonPrimitive(PASSWORD))
            put("confirmPassword", JsonPrimitive("$PASSWORD!"))
        }
        assertEquals("POST", request.method)
        assertEquals("/api/auth/activation", request.path)
        assertEquals(expectedBody, json.parseToJsonElement(request.body.orEmpty()))
    }

    @Test
    fun expiredOrUsedTokenOnActivate_isMapped() = runTest {
        val expired = activationFailure(410, errorBody("ACTIVATION_TOKEN_EXPIRED"))
        val used = activationFailure(410, errorBody("ACTIVATION_TOKEN_USED"))
        assertEquals(ActivationResult.Failure(ActivationError.TOKEN_EXPIRED), expired)
        assertEquals(ActivationResult.Failure(ActivationError.TOKEN_USED), used)
    }

    @Test
    fun invalidTokenOnActivate_isInvalidError() = runTest {
        val result = activationFailure(400, errorBody("ACTIVATION_TOKEN_INVALID"))
        assertEquals(ActivationResult.Failure(ActivationError.TOKEN_INVALID), result)
    }

    @Test
    fun validationErrors_mapByField() = runTest {
        val policy = activationFailure(400, errorBody("VALIDATION_ERROR", "password"))
        val mismatch = activationFailure(400, errorBody("VALIDATION_ERROR", "confirmPassword"))
        val both = activationFailure(400, errorBody("VALIDATION_ERROR", "confirmPassword", "password"))
        assertEquals(ActivationResult.Failure(ActivationError.PASSWORD_POLICY), policy)
        assertEquals(ActivationResult.Failure(ActivationError.PASSWORD_MISMATCH), mismatch)
        assertEquals(ActivationResult.Failure(ActivationError.PASSWORD_POLICY), both)
    }
}
