package com.example.paypulse.ui.app

import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder

private const val LINK_SCHEME = "paypulse"
private const val TOKEN_PARAMETER = "token"
private const val QUERY_ENCODING = "UTF-8"

internal fun tokenFromAppLink(link: String?, host: String): String? {
    if (link == null) return null
    return try {
        val uri = URI(link)
        if (!uri.scheme.equals(LINK_SCHEME, ignoreCase = true) || uri.host != host) return null
        queryParameter(uri.rawQuery, TOKEN_PARAMETER)?.takeIf { it.isNotBlank() }
    } catch (exception: URISyntaxException) {
        null
    } catch (exception: IllegalArgumentException) {
        null
    }
}

private fun queryParameter(rawQuery: String?, name: String): String? =
    rawQuery?.split('&')
        ?.map { it.split('=', limit = 2) }
        ?.firstOrNull { it.first() == name }
        ?.getOrNull(1)
        ?.let { URLDecoder.decode(it, QUERY_ENCODING) }
