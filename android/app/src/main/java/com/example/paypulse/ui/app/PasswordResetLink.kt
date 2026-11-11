package com.example.paypulse.ui.app

private const val PASSWORD_RESET_HOST = "reset-password"

fun passwordResetTokenFrom(link: String?): String? = tokenFromAppLink(link, PASSWORD_RESET_HOST)
