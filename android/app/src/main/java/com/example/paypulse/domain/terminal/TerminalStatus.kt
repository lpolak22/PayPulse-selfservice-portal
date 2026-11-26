package com.example.paypulse.domain.terminal

enum class TerminalStatus {
    PREPARED,
    AUTHENTICATED,
    ACTIVE,
    INACTIVE,
    DEACTIVATED,
    SUSPENDED,
    BLOCKED,
    UNKNOWN;

    companion object {
        fun fromApi(value: String?): TerminalStatus? {
            val trimmed = value?.trim() ?: return null
            return entries.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        }
    }
}