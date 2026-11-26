package com.example.paypulse.data.transaction

import com.example.paypulse.data.remote.ErrorResponseParser
import com.example.paypulse.data.remote.TransactionApi
import com.example.paypulse.data.remote.mapper.toTransactionDetails
import com.example.paypulse.data.remote.mapper.transactionDetailsErrorFrom
import com.example.paypulse.domain.transaction.TransactionDetailsError
import com.example.paypulse.domain.transaction.TransactionDetailsRepository
import com.example.paypulse.domain.transaction.TransactionDetailsResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class ApiTransactionDetailsRepository(
    private val transactionApi: TransactionApi,
    private val errorResponseParser: ErrorResponseParser
) : TransactionDetailsRepository {

    override suspend fun getDetails(id: Long): TransactionDetailsResult = try {
        TransactionDetailsResult.Success(transactionApi.getDetails(id).toTransactionDetails())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        TransactionDetailsResult.Failure(TransactionDetailsError.NETWORK)
    } catch (exception: HttpException) {
        TransactionDetailsResult.Failure(transactionDetailsErrorFrom(errorResponseParser.errorCode(exception)))
    } catch (exception: Exception) {
        TransactionDetailsResult.Failure(TransactionDetailsError.UNKNOWN)
    }
}
