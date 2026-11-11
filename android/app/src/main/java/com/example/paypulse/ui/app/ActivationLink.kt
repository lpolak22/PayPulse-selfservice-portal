package com.example.paypulse.ui.app

private const val ACTIVATION_HOST = "activate"

fun activationTokenFrom(link: String?): String? = tokenFromAppLink(link, ACTIVATION_HOST)
