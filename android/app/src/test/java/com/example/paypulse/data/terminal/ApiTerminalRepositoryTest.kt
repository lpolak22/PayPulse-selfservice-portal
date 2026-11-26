package com.example.paypulse.data.terminal

import com.example.paypulse.data.activation.CannedResponseInterceptor
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.domain.terminal.TerminalQuery
import com.example.paypulse.domain.terminal.TerminalResult
import com.example.paypulse.domain.terminal.TerminalStatus
import com.example.paypulse.domain.terminal.TerminalType
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.create
import java.io.IOException

private const val ONE_TERMINAL = """
    {"items":[{"id":451,"terminalId":"T-00451","type":"POS","status":"ACTIVE",
    "outlet":{"id":12,"name":"PM Ilica 12"},"merchant":{"id":3,"name":"Trgovina Kvadrat d.o.o."}}],
    "page":0,"pageSize":20,"total":1}
"""

class ApiTerminalRepositoryTest {

    private val server = CannedResponseInterceptor()
    private val urls = mutableListOf<HttpUrl>()
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain -> urls += chain.request().url; chain.proceed(chain.request()) }
        .addInterceptor(server)
        .build()
    private val repository = ApiTerminalRepository(
        createRetrofit("http://localhost/", createApiJson(), client).create()
    )

    private suspend fun failureFor(httpCode: Int, body: String = ""): TerminalResult {
        server.respond(httpCode, body)
        return repository.search(TerminalQuery())
    }

    @Test
    fun filters_sendStatusTypeAndPage() = runTest {
        server.respond(200, ONE_TERMINAL)
        repository.search(TerminalQuery(status = TerminalStatus.BLOCKED, type = TerminalType.SOFTPOS_V2, page = 3))

        val url = urls.single()
        assertEquals("/api/terminals", url.encodedPath)
        assertEquals("BLOCKED", url.queryParameter("status"))
        assertEquals("SoftPOS V2", url.queryParameter("type"))
        assertEquals("3", url.queryParameter("page"))
        assertEquals("20", url.queryParameter("pageSize"))
    }

    @Test
    fun noFilters_areNotSent() = runTest {
        server.respond(200, ONE_TERMINAL)
        repository.search(TerminalQuery())

        assertNull(urls.single().queryParameter("status"))
        assertNull(urls.single().queryParameter("type"))
    }

    @Test
    fun noOrganisationParameter_isSent() = runTest {
        server.respond(200, ONE_TERMINAL)
        repository.search(TerminalQuery(status = TerminalStatus.ACTIVE, type = TerminalType.POS))

        assertEquals(setOf("status", "type", "page", "pageSize"), urls.single().queryParameterNames)
    }

    @Test
    fun response_isSuccess() = runTest {
        server.respond(200, ONE_TERMINAL)
        val result = repository.search(TerminalQuery())

        assertTrue(result is TerminalResult.Success)
        assertEquals("T-00451", (result as TerminalResult.Success).page.items.single().terminalId)
    }

    @Test
    fun offline_isNetworkError() = runTest {
        server.failure = IOException("offline")
        assertEquals(TerminalResult.Failure(TerminalError.NETWORK), repository.search(TerminalQuery()))
    }

    @Test
    fun badRequest_isInvalidFilterError() = runTest {
        val body = """{"code":"VALIDATION_ERROR","message":"x","fieldErrors":[]}"""
        assertEquals(TerminalResult.Failure(TerminalError.INVALID_FILTER), failureFor(400, body))
    }

    @Test
    fun serverErrorOrMalformedReply_isUnknownError() = runTest {
        assertEquals(TerminalResult.Failure(TerminalError.UNKNOWN), failureFor(500))
        assertEquals(TerminalResult.Failure(TerminalError.UNKNOWN), failureFor(200, "not json"))
    }
}
