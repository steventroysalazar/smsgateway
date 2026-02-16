package org.traccar.gateway

data class GatewayMessage(
    val id: Long,
    val phone: String,
    val message: String,
    val date: Long
)

