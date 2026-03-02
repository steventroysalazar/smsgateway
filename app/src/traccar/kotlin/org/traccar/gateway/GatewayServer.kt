package org.traccar.gateway

import android.util.JsonReader
import org.eclipse.jetty.http.HttpHeaders
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.json.JSONArray
import org.json.JSONObject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class GatewayServer(
    port: Int,
    private val key: String?,
    private val handler: Handler
) : Server(port) {

    interface Handler {
        fun onSendMessage(phone: String, message: String, slot: Int?): String?
        fun onGetMessages(phone: String?, since: Long?, limit: Int?): List<GatewayMessage>
    }

    init {
        setHandler(object : AbstractHandler() {
            override fun handle(
                target: String,
                baseRequest: Request,
                request: HttpServletRequest,
                response: HttpServletResponse
            ) {
                if (request.method == "POST") {
                    response.contentType = "text/html; charset=utf-8"
                    handlePost(request, response)
                } else {
                    handleGet(target, request, response)
                }

                baseRequest.isHandled = true
            }
        })
    }

    private fun isAuthorized(request: HttpServletRequest): Boolean {
        return request.getHeader(HttpHeaders.AUTHORIZATION) == key
    }

    private fun handlePost(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (!isAuthorized(request)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        var phone: String? = null
        var message: String? = null
        var slot: Int? = null

        val reader = JsonReader(request.reader)
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "to" -> phone = reader.nextString()
                "message" -> message = reader.nextString()
                "slot" -> slot = reader.nextInt()
                else -> reader.skipValue()
            }
        }

        val result = if (phone != null && message != null) {
            handler.onSendMessage(phone, message, slot)
        } else {
            "Missing phone or message"
        }

        if (result == null) {
            response.status = HttpServletResponse.SC_OK
        } else {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            response.writer.print(result)
        }
    }

    private fun handleGet(
        target: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (target == "/messages") {
            if (!isAuthorized(request)) {
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return
            }

            val phone = request.getParameter("phone")
            val since = request.getParameter("since")?.toLongOrNull()
            val limit = request.getParameter("limit")?.toIntOrNull()

            val messages = handler.onGetMessages(phone, since, limit)
            val jsonArray = JSONArray()
            messages.forEach { message ->
                jsonArray.put(
                    JSONObject()
                        .put("id", message.id)
                        .put("from", message.phone)
                        .put("message", message.message)
                        .put("date", message.date)
                )
            }

            response.contentType = "application/json; charset=utf-8"
            response.status = HttpServletResponse.SC_OK
            response.writer.print(jsonArray.toString())
            return
        }

        response.contentType = "text/html; charset=utf-8"
        response.writer.print(
            """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body>
                <p>Send SMS using following API:</p>
                <pre>
                POST /
                {
                    "to": "+10000000000",
                    "message": "Your message"
                }
                </pre>
                <p>Read incoming SMS replies:</p>
                <pre>
                GET /messages?phone=+10000000000&amp;since=1700000000000&amp;limit=100
                </pre>
            </body>
            </html>
            """.trimIndent()
        )
    }

}
