package com.example.paypulse.data.merchant

import com.example.paypulse.data.activation.CannedResponseInterceptor
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.merchant.MerchantDetails
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.merchant.MerchantDetailsResult
import com.example.paypulse.domain.merchant.MerchantStatus
import com.example.paypulse.domain.merchant.MerchantTerminal
import com.example.paypulse.domain.merchant.RelatedItems
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletStatus
import com.example.paypulse.domain.terminal.TerminalStatus
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.create
import java.io.IOException

private const val FULL_DETAILS = """
    {"id":7,"merchantId":"TR-100245","name":"Trgovina Kvadrat d.o.o.","registrationNumber":"12345678901",
    "status":"ACTIVE",
    "outlets":{"items":[{"id":11,"outletId":"PM-0001","name":"PM Ilica 12","address":"Ilica 12","city":"Zagreb",
    "postalCode":"10000","status":"active","terminalCount":3}],"total":4,"href":"/api/merchants/7/outlets"},
    "terminals":{"items":[{"id":21,"terminalId":"T0001","outlet":{"id":11,"name":"PM Ilica 12"},"status":"BLOCKED"}],
    "total":9,"href":"/api/terminals?merchantId=7"}}
"""

class ApiMerchantRepositoryDetailsTest {

    private val server = CannedResponseInterceptor()
    private val urls = mutableListOf<HttpUrl>()
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain -> urls += chain.request().url; chain.proceed(chain.request()) }
        .addInterceptor(server)
        .build()
    private val repository = ApiMerchantRepository(
        createRetrofit("http://localhost/", createApiJson(), client).create()
    )

    private suspend fun detailsOf(json: String): MerchantDetails {
        server.respond(200, json)
        return (repository.details(7) as MerchantDetailsResult.Success).details
    }

    private suspend fun failureFor(httpCode: Int, body: String = ""): MerchantDetailsResult {
        server.respond(httpCode, body)
        return repository.details(7)
    }

    @Test
    fun details_requestsMerchantById() = runTest {
        server.respond(200, FULL_DETAILS)
        repository.details(7)

        assertEquals("/api/merchants/7", urls.single().encodedPath)
    }

    @Test
    fun response_isMappedToDetailsWithOutletsAndTerminals() = runTest {
        val expected = MerchantDetails(
            id = 7,
            merchantId = "TR-100245",
            name = "Trgovina Kvadrat d.o.o.",
            oib = "12345678901",
            status = MerchantStatus.ACTIVE,
            outlets = RelatedItems(
                items = listOf(Outlet(11, "PM-0001", "PM Ilica 12", "Ilica 12", "Zagreb", "10000", OutletStatus.ACTIVE, 3)),
                total = 4
            ),
            terminals = RelatedItems(
                items = listOf(MerchantTerminal(21, "T0001", "PM Ilica 12", TerminalStatus.BLOCKED)),
                total = 9
            )
        )
        assertEquals(expected, detailsOf(FULL_DETAILS))
    }

    @Test
    fun missingListsAndOptionalFields_areEmptyOrNull() = runTest {
        val details = detailsOf("""{"id":7,"name":"A","registrationNumber":"1","merchantId":" ","status":"CLOSED"}""")

        assertNull(details.merchantId)
        assertNull(details.status)
        assertEquals(RelatedItems<Outlet>(emptyList(), 0), details.outlets)
        assertEquals(RelatedItems<MerchantTerminal>(emptyList(), 0), details.terminals)
    }

    @Test
    fun blankOutletAndTerminalFields_areNull() = runTest {
        val details = detailsOf(
            """{"id":7,"name":"A","registrationNumber":"1",
            "outlets":{"items":[{"id":1,"name":"PM","address":" ","city":"","status":"OPEN"}],"total":1},
            "terminals":{"items":[{"id":2,"terminalId":" ","status":"weird"}],"total":1}}"""
        )

        assertEquals(Outlet(1, null, "PM", null, null, null, null, 0), details.outlets.items.single())
        assertEquals(MerchantTerminal(2, null, null, null), details.terminals.items.single())
    }

    @Test
    fun totalSmallerThanItems_isRaisedToItemCount() = runTest {
        val details = detailsOf(
            """{"id":7,"name":"A","registrationNumber":"1","outlets":{"items":[{"id":1,"name":"PM"}],"total":0}}"""
        )
        assertEquals(1L, details.outlets.total)
    }

    @Test
    fun notFound_isNotFoundError() = runTest {
        val body = """{"code":"NOT_FOUND","message":"Merchant not found"}"""
        assertEquals(MerchantDetailsResult.Failure(MerchantDetailsError.NOT_FOUND), failureFor(404, body))
    }

    @Test
    fun offline_isNetworkError() = runTest {
        server.failure = IOException("offline")
        assertEquals(MerchantDetailsResult.Failure(MerchantDetailsError.NETWORK), repository.details(7))
    }

    @Test
    fun serverErrorOrMalformedReply_isUnknownError() = runTest {
        assertEquals(MerchantDetailsResult.Failure(MerchantDetailsError.UNKNOWN), failureFor(500))
        assertEquals(MerchantDetailsResult.Failure(MerchantDetailsError.UNKNOWN), failureFor(403))
        assertEquals(MerchantDetailsResult.Failure(MerchantDetailsError.UNKNOWN), failureFor(200, "not json"))
    }
}
