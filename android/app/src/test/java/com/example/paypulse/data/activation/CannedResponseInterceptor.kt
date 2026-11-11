package com.example.paypulse.data.activation

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

class CannedResponseInterceptor : Interceptor {

    class RecordedRequest(val method: String, val path: String, val body: String?)

    private var responseCode = 200
    private var responseBody = ""
    var failure: IOException? = null
    val requests = mutableListOf<RecordedRequest>()

    fun respond(code: Int, body: String = "") {
        responseCode = code
        responseBody = body
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        requests += RecordedRequest(request.method, request.url.encodedPath, request.body?.let { readBody(it) })
        failure?.let { throw it }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(responseCode)
            .message("")
            .body(responseBody.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun readBody(body: RequestBody): String = Buffer().also { body.writeTo(it) }.readUtf8()
}
