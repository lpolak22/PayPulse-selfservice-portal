package com.example.paypulse.data.outlet

import com.example.paypulse.data.activation.CannedResponseInterceptor
import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.createRetrofit
import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletCitiesResult
import com.example.paypulse.domain.outlet.OutletError
import com.example.paypulse.domain.outlet.OutletPage
import com.example.paypulse.domain.outlet.OutletQuery
import com.example.paypulse.domain.outlet.OutletResult
import com.example.paypulse.domain.outlet.OutletStatus
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.create
import java.io.IOException

private const val ONE_OUTLET = """
    {"items":[{"id":12,"outletId":"MID-ZG-001","name":"Konzum Ilica","address":"Ilica 1","city":"Zagreb",
    "postalCode":"10000","status":"ACTIVE","terminalCount":3}],"page":0,"pageSize":20,"total":1}
"""

class ApiOutletRepositoryTest {

    private val server = CannedResponseInterceptor()
    private val urls = mutableListOf<HttpUrl>()
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain -> urls += chain.request().url; chain.proceed(chain.request()) }
        .addInterceptor(server)
        .build()
    private val repository = ApiOutletRepository(
        createRetrofit("http://localhost/", createApiJson(), client).create()
    )

    private suspend fun firstOutlet(outletJson: String): Outlet {
        server.respond(200, """{"items":[$outletJson],"page":0,"pageSize":20,"total":1}""")
        return (repository.search(OutletQuery(merchantId = 7)) as OutletResult.Success).page.items.single()
    }

    @Test
    fun search_sendsMerchantTrimmedTextCityAndPage() = runTest {
        server.respond(200, ONE_OUTLET)
        repository.search(OutletQuery(merchantId = 7, search = "  ilica ", city = " Rijeka ", page = 2))

        val url = urls.single()
        assertEquals("/api/merchants/7/outlets", url.encodedPath)
        assertEquals("ilica", url.queryParameter("search"))
        assertEquals("Rijeka", url.queryParameter("city"))
        assertEquals("2", url.queryParameter("page"))
        assertEquals("20", url.queryParameter("pageSize"))
    }

    @Test
    fun blankSearchAndNoCity_areNotSent() = runTest {
        server.respond(200, ONE_OUTLET)
        repository.search(OutletQuery(merchantId = 7, search = "   "))

        assertNull(urls.single().queryParameter("search"))
        assertNull(urls.single().queryParameter("city"))
    }

    @Test
    fun response_isMappedToOutlets() = runTest {
        server.respond(200, ONE_OUTLET)
        val expected = OutletPage(
            items = listOf(
                Outlet(12, "MID-ZG-001", "Konzum Ilica", "Ilica 1", "Zagreb", "10000", OutletStatus.ACTIVE, 3)
            ),
            page = 0,
            pageSize = 20,
            total = 1
        )
        assertEquals(OutletResult.Success(expected), repository.search(OutletQuery(merchantId = 7)))
    }

    @Test
    fun unknownStatus_isMappedToNull() = runTest {
        assertNull(firstOutlet("""{"id":1,"name":"A","status":"CLOSED"}""").status)
    }

    @Test
    fun missingOrBlankTexts_areNull() = runTest {
        val outlet = firstOutlet("""{"id":1,"name":"A","outletId":" ","address":"","city":"  "}""")

        assertNull(outlet.outletId)
        assertNull(outlet.address)
        assertNull(outlet.city)
        assertNull(outlet.postalCode)
    }

    @Test
    fun cities_areReadForTheMerchant() = runTest {
        server.respond(200, """["Rijeka","Split","Zagreb"]""")

        assertEquals(OutletCitiesResult.Success(listOf("Rijeka", "Split", "Zagreb")), repository.cities(7))
        assertEquals("/api/merchants/7/outlets/cities", urls.single().encodedPath)
    }

    @Test
    fun offline_isNetworkError() = runTest {
        server.failure = IOException("offline")

        assertEquals(OutletResult.Failure(OutletError.NETWORK), repository.search(OutletQuery(merchantId = 7)))
        assertEquals(OutletCitiesResult.Failure(OutletError.NETWORK), repository.cities(7))
    }

    @Test
    fun notFound_isMerchantNotFoundError() = runTest {
        server.respond(404, """{"code":"NOT_FOUND","message":"x"}""")

        assertEquals(
            OutletResult.Failure(OutletError.MERCHANT_NOT_FOUND),
            repository.search(OutletQuery(merchantId = 7))
        )
        assertEquals(OutletCitiesResult.Failure(OutletError.MERCHANT_NOT_FOUND), repository.cities(7))
    }

    @Test
    fun serverErrorOrMalformedReply_isUnknownError() = runTest {
        server.respond(500)
        assertEquals(OutletResult.Failure(OutletError.UNKNOWN), repository.search(OutletQuery(merchantId = 7)))

        server.respond(200, "not json")
        assertEquals(OutletCitiesResult.Failure(OutletError.UNKNOWN), repository.cities(7))
    }
}
