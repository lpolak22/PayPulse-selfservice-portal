package com.example.paypulse.ui.terminals

import com.example.paypulse.domain.terminal.Terminal
import com.example.paypulse.domain.terminal.TerminalPage
import com.example.paypulse.domain.terminal.TerminalQuery
import com.example.paypulse.domain.terminal.TerminalRepository
import com.example.paypulse.domain.terminal.TerminalResult
import com.example.paypulse.domain.terminal.TerminalStatus

class StubTerminalRepository : TerminalRepository {

    val queries = mutableListOf<TerminalQuery>()
    private val queued = ArrayDeque<TerminalResult>()
    var defaultResult: TerminalResult = successPage(terminals(1..2), total = 2)

    fun enqueue(vararg results: TerminalResult) {
        queued.addAll(results)
    }

    override suspend fun search(query: TerminalQuery): TerminalResult {
        queries += query
        return queued.removeFirstOrNull() ?: defaultResult
    }

    companion object {
        fun terminal(id: Long) = Terminal(
            id = id,
            terminalId = "T-$id",
            type = "POS",
            status = TerminalStatus.ACTIVE,
            rawStatus = "ACTIVE",
            outletName = "Prodajno mjesto $id",
            merchantName = "Trgovac $id"
        )

        fun terminals(ids: IntRange): List<Terminal> = ids.map { terminal(it.toLong()) }

        fun successPage(items: List<Terminal>, total: Long, page: Int = 0, pageSize: Int = 20) =
            TerminalResult.Success(TerminalPage(items, page, pageSize, total))
    }
}
