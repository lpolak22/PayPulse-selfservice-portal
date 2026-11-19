package com.example.paypulse.data.outlet

import com.example.paypulse.data.remote.OutletApi
import com.example.paypulse.data.remote.mapper.toOutletPage
import com.example.paypulse.domain.outlet.OutletCitiesResult
import com.example.paypulse.domain.outlet.OutletError
import com.example.paypulse.domain.outlet.OutletQuery
import com.example.paypulse.domain.outlet.OutletRepository
import com.example.paypulse.domain.outlet.OutletResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

class ApiOutletRepository(private val outletApi: OutletApi) : OutletRepository {

    override suspend fun search(query: OutletQuery): OutletResult = fetching(OutletResult::Failure) {
        val response = outletApi.search(
            merchantId = query.merchantId,
            search = query.search.trim().ifEmpty { null },
            city = query.city?.trim()?.ifEmpty { null },
            page = query.page,
            pageSize = query.pageSize
        )
        OutletResult.Success(response.toOutletPage())
    }

    override suspend fun cities(merchantId: Long): OutletCitiesResult = fetching(OutletCitiesResult::Failure) {
        OutletCitiesResult.Success(outletApi.cities(merchantId))
    }

    private suspend fun <T> fetching(failure: (OutletError) -> T, request: suspend () -> T): T = try {
        request()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        failure(OutletError.NETWORK)
    } catch (exception: HttpException) {
        failure(exception.toOutletError())
    } catch (exception: Exception) {
        failure(OutletError.UNKNOWN)
    }

    private fun HttpException.toOutletError(): OutletError =
        if (code() == HttpURLConnection.HTTP_NOT_FOUND) OutletError.MERCHANT_NOT_FOUND else OutletError.UNKNOWN
}
