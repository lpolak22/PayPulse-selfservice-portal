package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.createApiJson
import com.example.paypulse.data.remote.dto.PageResponseDto
import com.example.paypulse.data.remote.dto.TerminalDto
import com.example.paypulse.domain.terminal.Terminal
import com.example.paypulse.domain.terminal.TerminalPage
import com.example.paypulse.domain.terminal.TerminalStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TerminalMapperTest {

    private val json = createApiJson()

    private fun page(body: String): TerminalPage =
        json.decodeFromString<PageResponseDto<TerminalDto>>(body).toTerminalPage()

    private fun terminal(type: String = "POS", status: String = "ACTIVE"): Terminal = page(
        """{"items":[{"id":1,"terminalId":"T-1","type":"$type","status":"$status",
        "outlet":{"id":2,"name":"PM Ilica"},"merchant":{"id":3,"name":"Kvadrat d.o.o."}}],
        "page":0,"pageSize":20,"total":1}"""
    ).items.single()

    @Test
    fun response_isMappedToTerminals() {
        val body = """{"items":[{"id":451,"terminalId":"T-00451","type":"SoftPOS V1","status":"ACTIVE",
            "outlet":{"id":12,"name":"PM Ilica 12"},"merchant":{"id":3,"name":"Trgovina Kvadrat d.o.o."}}],
            "page":1,"pageSize":20,"total":21}"""
        val expected = TerminalPage(
            items = listOf(
                Terminal(451, "T-00451", "SoftPOS V1", TerminalStatus.ACTIVE, "ACTIVE", "PM Ilica 12", "Trgovina Kvadrat d.o.o.")
            ),
            page = 1,
            pageSize = 20,
            total = 21
        )
        assertEquals(expected, page(body))
    }

    @Test
    fun everyKnownStatus_isMapped() {
        TerminalStatus.entries.forEach { status ->
            assertEquals(status, terminal(status = status.name).status)
        }
    }

    @Test
    fun unknownStatus_keepsRawTextAndNullStatus() {
        val terminal = terminal(status = "RETIRED")
        assertNull(terminal.status)
        assertEquals("RETIRED", terminal.rawStatus)
    }

    @Test
    fun statusCase_isIgnored() {
        assertEquals(TerminalStatus.SUSPENDED, terminal(status = " suspended ").status)
    }

    @Test
    fun unknownType_isKeptAsIs() {
        assertEquals("Kiosk X", terminal(type = "Kiosk X").type)
    }

    @Test
    fun missingItems_isEmptyPage() {
        assertEquals(TerminalPage(emptyList(), 0, 20, 0), page("""{"page":0,"pageSize":20,"total":0}"""))
    }
}
