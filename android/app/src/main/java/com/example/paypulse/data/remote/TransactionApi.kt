package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.data.remote.dto.TransactionDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionApi {
    @GET("api/transactions")
    suspend fun search(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("status") status: List<String>,
        @Query("type") type: List<String>,
        @Query("cardBrand") cardBrand: List<String>,
        @Query("salesChannel") salesChannel: List<String>,
        @Query("authCode") authCode: String?,
        @Query("rrn") rrn: String?,
        @Query("sort") sort: String,
        @Query("direction") direction: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PageResponseDto<TransactionDto>
}
