package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.OutletDto
import com.example.paypulse.data.remote.dto.PageResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OutletApi {
    @GET("api/merchants/{merchantId}/outlets")
    suspend fun search(
        @Path("merchantId") merchantId: Long,
        @Query("search") search: String?,
        @Query("city") city: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PageResponseDto<OutletDto>

    @GET("api/merchants/{merchantId}/outlets/cities")
    suspend fun cities(@Path("merchantId") merchantId: Long): List<String>
}
