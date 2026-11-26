package com.example.paypulse.domain.outlet

data class Outlet(
    val id: Long,
    val outletId: String?,
    val name: String,
    val address: String?,
    val city: String?,
    val postalCode: String?,
    val status: OutletStatus?,
    val terminalCount: Long
)
