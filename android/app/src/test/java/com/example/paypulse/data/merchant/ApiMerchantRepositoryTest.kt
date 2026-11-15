package com.example.paypulse.data.merchant

import com.example.paypulse.data.activation.CannedResponseInterceptor
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.merchant.Merchant
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantPage
import com.example.paypulse.domain.merchant.MerchantQuery
import com.example.paypulse.domain.merchant.MerchantResult
import com.example.paypulse.domain.merchant.MerchantStatus
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.create
import java.io.IOException

private const val ONE_MERCHANT = """
    {"items":[{"id":7,"merchantId":"7PAYMENTS","name":"7 PAYMENTS d.o.o","registrationNumber":"34855856319",
    "status":"active","outletCount":27,"terminalCount":480}],"page":0,"pageSize":20,"total":1}
"""

class ApiMerchantRepositoryTest {

    private val server = CannedResponseInterceptor()
    private val urls = mutableListOf<HttpUrl>()
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain -> urls += chain.request().url; chain.proceed(chain.request()) }
        .addInterceptor(server)
        .build()
    private val repository = ApiMerchantRepository(
        createRetrofit("http://localhost/", createApiJson(), client).create()
    )

    private fun pageWith(merchantJson: String) =
        """{"items":[$merchantJson],"page":0,"pageSize":20,"total":1}"""

    private suspend fun firstMerchant(merchantJson: String): Merchant {
        server.respond(200, pageWith(merchantJson))
        return (repository.search(MerchantQuery()) as MerchantResult.Success).page.items.single()
    }

    private suspend fun failureFor(httpCode: Int, body: String = ""): MerchantResult {
        server.respond(httpCode, body)
        return repository.search(MerchantQuery())
    }

    @Test
    fun search_sendsTrimmedTextStatusAndPage() = runTest {
        server.respond(200, ONE_MERCHANT)
        repository.search(MerchantQuery(search = "  konzum ", status = MerchantStatus.HOLD, page = 2, pageSize = 20))

        val url = urls.single()
        assertEquals("/api/merchants", url.encodedPath)
        assertEquals("konzum", url.queryParameter("search"))
        assertEquals("HOLD", url.queryParameter("status"))
        assertEquals("2", url.queryParameter("page"))
        assertEquals("20", url.queryParameter("pageSize"))
    }

    @Test
    fun blankSearchAndNoStatus_areNotSent() = runTest {
        server.respond(200, ONE_MERCHANT)
        repository.search(MerchantQuery(search = "   "))

        assertNull(urls.single().queryParameter("search"))
        assertNull(urls.single().queryParameter("status"))
    }

    @Test
    fun response_isMappedToMerchants() = runTest {
        server.respond(200, ONE_MERCHANT)
        val expected = MerchantPage(
            items = listOf(Merchant(7, "7PAYMENTS", "7 PAYMENTS d.o.o", "34855856319", MerchantStatus.ACTIVE, 27, 480)),
            page = 0,
            pageSize = 20,
            total = 1
        )
        assertEquals(MerchantResult.Success(expected), repository.search(MerchantQuery()))
    }

    @Test
    fun unknownStatus_isMappedToNull() = runTest {
        val merchant = firstMerchant("""{"id":1,"name":"A","registrationNumber":"1","status":"CLOSED"}""")
        assertNull(merchant.status)
    }

    @Test
    fun missingOrBlankMerchantId_isNull() = runTest {
        assertNull(firstMerchant("""{"id":1,"name":"A","registrationNumber":"1"}""").merchantId)
        assertNull(firstMerchant("""{"id":1,"merchantId":" ","name":"A","registrationNumber":"1"}""").merchantId)
    }

    @Test
    fun offline_isNetworkError() = runTest {
        server.failure = IOException("offline")
        assertEquals(MerchantResult.Failure(MerchantError.NETWORK), repository.search(MerchantQuery()))
    }

    @Test
    fun badRequest_isInvalidFilterError() = runTest {
        val body = """{"code":"VALIDATION_ERROR","message":"x","fieldErrors":[]}"""
        assertEquals(MerchantResult.Failure(MerchantError.INVALID_FILTER), failureFor(400, body))
    }

    @Test
    fun serverErrorOrMalformedReply_isUnknownError() = runTest {
        assertEquals(MerchantResult.Failure(MerchantError.UNKNOWN), failureFor(500))
        assertEquals(MerchantResult.Failure(MerchantError.UNKNOWN), failureFor(200, "not json"))
    }
}
