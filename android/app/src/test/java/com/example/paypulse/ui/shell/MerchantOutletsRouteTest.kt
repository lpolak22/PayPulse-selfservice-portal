package com.example.paypulse.ui.shell

import com.example.paypulse.ui.outlets.OutletsViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantOutletsRouteTest {

    @Test
    fun routeArguments_matchTheKeysTheOutletListReads() {
        assertEquals(OutletsViewModel.MERCHANT_ID_KEY, ShellRoute.MerchantOutlets::merchantId.name)
        assertEquals(OutletsViewModel.MERCHANT_NAME_KEY, ShellRoute.MerchantOutlets::merchantName.name)
    }
}
