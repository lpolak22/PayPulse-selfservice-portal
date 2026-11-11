package com.example.paypulse.data.transaction

import com.example.paypulse.data.remote.TransactionApi
import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.data.remote.dto.TransactionDto
import com.example.paypulse.data.remote.mapper.toTransactionPage
import com.example.paypulse.domain.dashboard.DashboardPeriod
import com.example.paypulse.domain.transaction.TransactionError
import com.example.paypulse.domain.transaction.TransactionQuery
import com.example.paypulse.domain.transaction.TransactionRepository
import com.example.paypulse.domain.transaction.TransactionResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import java.time.LocalDate
import java.util.Locale

class ApiTransactionRepository(private val transactionApi: TransactionApi) : TransactionRepository {

    override suspend fun search(query: TransactionQuery): TransactionResult = try {
        TransactionResult.Success(query.fetch().toTransactionPage())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        TransactionResult.Failure(TransactionError.NETWORK)
    } catch (exception: HttpException) {
        TransactionResult.Failure(exception.toTransactionError())
    } catch (exception: Exception) {
        TransactionResult.Failure(TransactionError.UNKNOWN)
    }

    private suspend fun TransactionQuery.fetch(): PageResponseDto<TransactionDto> = transactionApi.search(
        from = filters.dateRange.from.toQueryInstant(),
        to = filters.dateRange.to.plusDays(1).toQueryInstant(),
        status = filters.statuses.names(),
        type = filters.types.names(),
        cardBrand = filters.cardBrands.names(),
        salesChannel = filters.salesChannels.names(),
        authCode = filters.authCode,
        rrn = filters.rrn,
        sort = sort.field.name.lowercase(Locale.ROOT),
        direction = sort.direction.name.lowercase(Locale.ROOT),
        page = page,
        pageSize = pageSize
    )

    private fun LocalDate.toQueryInstant(): String = atStartOfDay(DashboardPeriod.ZONE).toInstant().toString()

    private fun Set<Enum<*>>.names(): List<String> = map { it.name }.sorted()

    private fun HttpException.toTransactionError(): TransactionError =
        if (code() == HttpURLConnection.HTTP_BAD_REQUEST) TransactionError.INVALID_FILTER else TransactionError.UNKNOWN
}
