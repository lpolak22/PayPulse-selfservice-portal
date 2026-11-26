package com.example.paypulse.domain.terminal

enum class TerminalType(val apiValue: String) {
    SOFTPOS_V1("SoftPOS V1"),
    SOFTPOS_V2("SoftPOS V2"),
    POS("POS"),
    MOBILE("MOBILE"),
    ECR("ECR"),
    VIRTUAL("VIRTUAL")
}
