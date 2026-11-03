package com.example.paypulse.domain.dashboard

import java.math.BigDecimal

data class CardBrandTotal(val brand: CardBrand, val amount: BigDecimal, val count: Int)

data class AcquirerTotal(val acquirer: String, val amount: BigDecimal, val count: Int)
