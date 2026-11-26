package com.example.paypulse.data.remote

import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.data.remote.dto.TerminalDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TerminalApi {
    @GET("api/terminals")
    suspend fun search(
        @Query("status") status: String?,
        @Query("type") type: String?,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): PageResponseDto<TerminalDto>
}
