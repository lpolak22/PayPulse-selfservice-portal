package com.example.paypulse.data.remote.mapper

internal inline fun <reified T : Enum<T>> enumOrNull(value: String?): T? =
    enumValues<T>().firstOrNull { it.name == value }
