package com.example.paypulse.data.remote.mapper

import com.example.paypulse.data.remote.TransactionApiContract
import com.example.paypulse.domain.transaction.TransactionDetailsError

fun transactionDetailsErrorFrom(code: String?): TransactionDetailsError =
    if (code == TransactionApiContract.CODE_NOT_FOUND) TransactionDetailsError.NOT_FOUND
    else TransactionDetailsError.UNKNOWN
