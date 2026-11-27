package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.data.remote.dto.TerminalDto
import com.example.paypulse.domain.terminal.Terminal
import com.example.paypulse.domain.terminal.TerminalPage
import com.example.paypulse.domain.terminal.TerminalStatus

fun PageResponseDto<TerminalDto>.toTerminalPage(): TerminalPage = TerminalPage(
    items = items.map { it.toTerminal() },
    page = page,
    pageSize = pageSize,
    total = total
)

fun TerminalDto.toTerminal(): Terminal = Terminal(
    id = id,
    terminalId = terminalId,
    type = type.trim(),
    status = TerminalStatus.fromApi(status),
    rawStatus = status.trim(),
    outletName = outlet.name,
    merchantName = merchant.name
)
