package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.MerchantDto
import com.example.paypulse.data.remote.dto.PageResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MerchantApi {
    @GET("api/merchants")
    suspend fun search(
        @Query("search") search: String?,
        @Query("status") status: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PageResponseDto<MerchantDto>
}
