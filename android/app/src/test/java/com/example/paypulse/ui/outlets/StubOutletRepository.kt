package com.example.paypulse.ui.outlets

import com.example.paypulse.domain.outlet.Outlet
import com.example.paypulse.domain.outlet.OutletCitiesResult
import com.example.paypulse.domain.outlet.OutletPage
import com.example.paypulse.domain.outlet.OutletQuery
import com.example.paypulse.domain.outlet.OutletRepository
import com.example.paypulse.domain.outlet.OutletResult
import com.example.paypulse.domain.outlet.OutletStatus

class StubOutletRepository : OutletRepository {

    val queries = mutableListOf<OutletQuery>()
    val cityRequests = mutableListOf<Long>()
    private val queued = ArrayDeque<OutletResult>()
    var defaultResult: OutletResult = successPage(outlets(1..2), total = 2)
    var citiesResult: OutletCitiesResult = OutletCitiesResult.Success(listOf("Rijeka", "Split", "Zagreb"))

    fun enqueue(vararg results: OutletResult) {
        queued.addAll(results)
    }

    override suspend fun search(query: OutletQuery): OutletResult {
        queries += query
        return queued.removeFirstOrNull() ?: defaultResult
    }

    override suspend fun cities(merchantId: Long): OutletCitiesResult {
        cityRequests += merchantId
        return citiesResult
    }

    companion object {
        fun outlet(id: Long) = Outlet(
            id = id,
            outletId = "MID-$id",
            name = "Prodajno mjesto $id",
            address = "Ulica $id",
            city = "Zagreb",
            postalCode = "10000",
            status = OutletStatus.ACTIVE,
            terminalCount = 2
        )

        fun outlets(ids: IntRange): List<Outlet> = ids.map { outlet(it.toLong()) }

        fun successPage(items: List<Outlet>, total: Long, page: Int = 0, pageSize: Int = 20) =
            OutletResult.Success(OutletPage(items, page, pageSize, total))
    }
}
