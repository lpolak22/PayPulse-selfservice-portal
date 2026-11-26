package com.example.paypulse.data.terminal

import com.example.paypulse.data.remote.TerminalApi
import com.example.paypulse.data.remote.mapper.toTerminalPage
import com.example.paypulse.domain.terminal.TerminalError
import com.example.paypulse.domain.terminal.TerminalQuery
import com.example.paypulse.domain.terminal.TerminalRepository
import com.example.paypulse.domain.terminal.TerminalResult
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

class ApiTerminalRepository(private val terminalApi: TerminalApi) : TerminalRepository {

    override suspend fun search(query: TerminalQuery): TerminalResult = try {
        val response = terminalApi.search(
            status = query.status?.name,
            type = query.type?.apiValue,
            page = query.page,
            pageSize = query.pageSize
        )
        TerminalResult.Success(response.toTerminalPage())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: IOException) {
        TerminalResult.Failure(TerminalError.NETWORK)
    } catch (exception: HttpException) {
        TerminalResult.Failure(exception.toTerminalError())
    } catch (exception: Exception) {
        TerminalResult.Failure(TerminalError.UNKNOWN)
    }

    private fun HttpException.toTerminalError(): TerminalError =
        if (code() == HttpURLConnection.HTTP_BAD_REQUEST) TerminalError.INVALID_FILTER else TerminalError.UNKNOWN
}
