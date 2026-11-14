package com.example.paypulse.data.merchant

import com.example.paypulse.data.remote.MerchantApi
import com.example.paypulse.data.remote.mapper.toMerchantPage
import com.example.paypulse.domain.merchant.MerchantError
import com.example.paypulse.domain.merchant.MerchantQuery
import com.example.paypulse.domain.merchant.MerchantRepository
import com.example.paypulse.domain.merchant.MerchantResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

class ApiMerchantRepository(private val merchantApi: MerchantApi) : MerchantRepository {

    override suspend fun search(query: MerchantQuery): MerchantResult = try {
        val response = merchantApi.search(
            search = query.search.trim().ifEmpty { null },
            status = query.status?.name,
            page = query.page,
            pageSize = query.pageSize
        )
        MerchantResult.Success(response.toMerchantPage())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        MerchantResult.Failure(MerchantError.NETWORK)
    } catch (exception: HttpException) {
        MerchantResult.Failure(exception.toMerchantError())
    } catch (exception: Exception) {
        MerchantResult.Failure(MerchantError.UNKNOWN)
    }

    private fun HttpException.toMerchantError(): MerchantError =
        if (code() == HttpURLConnection.HTTP_BAD_REQUEST) MerchantError.INVALID_FILTER else MerchantError.UNKNOWN
}
