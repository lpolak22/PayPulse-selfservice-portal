package com.example.paypulse.ui.merchants

import com.example.paypulse.domain.merchant.Merchant
import com.example.paypulse.domain.merchant.MerchantDetailsError
import com.example.paypulse.domain.merchant.MerchantDetailsResult
import com.example.paypulse.domain.merchant.MerchantPage
import com.example.paypulse.domain.merchant.MerchantQuery
import com.example.paypulse.domain.merchant.MerchantRepository
import com.example.paypulse.domain.merchant.MerchantResult
import com.example.paypulse.domain.merchant.MerchantStatus

class StubMerchantRepository : MerchantRepository {

    val queries = mutableListOf<MerchantQuery>()
    private val queued = ArrayDeque<MerchantResult>()
    var defaultResult: MerchantResult = successPage(merchants(1..2), total = 2)

    fun enqueue(vararg results: MerchantResult) {
        queued.addAll(results)
    }

    override suspend fun search(query: MerchantQuery): MerchantResult {
        queries += query
        return queued.removeFirstOrNull() ?: defaultResult
    }

    val detailsRequests = mutableListOf<Long>()
    private val queuedDetails = ArrayDeque<MerchantDetailsResult>()
    var defaultDetailsResult: MerchantDetailsResult = MerchantDetailsResult.Failure(MerchantDetailsError.NOT_FOUND)

    fun enqueueDetails(vararg results: MerchantDetailsResult) {
        queuedDetails.addAll(results)
    }

    override suspend fun details(id: Long): MerchantDetailsResult {
        detailsRequests += id
        return queuedDetails.removeFirstOrNull() ?: defaultDetailsResult
    }

    companion object {
        fun merchant(id: Long) = Merchant(
            id = id,
            merchantId = "SP-$id",
            name = "Trgovac $id",
            oib = id.toString().padStart(11, '0'),
            status = MerchantStatus.ACTIVE,
            outletCount = 1,
            terminalCount = 2
        )

        fun merchants(ids: IntRange): List<Merchant> = ids.map { merchant(it.toLong()) }

        fun successPage(items: List<Merchant>, total: Long, page: Int = 0, pageSize: Int = 20) =
            MerchantResult.Success(MerchantPage(items, page, pageSize, total))
    }
}
