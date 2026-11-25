package com.example.paypulse.ui.merchants.details

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MerchantDetailsLabelsTest {

    @Test
    fun initials_useTheFirstTwoWordsAndSkipLegalSuffixes() {
        assertEquals("TK", merchantInitials("Trgovina Kvadrat d.o.o."))
        assertEquals("QS", merchantInitials("Quick Shop d.o.o. Zagreb"))
        assertEquals("7P", merchantInitials("7 PAYMENTS d.o.o"))
        assertEquals("KJ", merchantInitials("kavana  jelačić"))
    }

    @Test
    fun initials_fallBackToTheFirstCharacter() {
        assertEquals("K", merchantInitials("Kvadrat"))
        assertEquals("&", merchantInitials(" & d.o.o."))
    }

    @Test
    fun addressLine_joinsStreetPostalCodeAndCity() {
        assertEquals("Ilica 12, 10000 Zagreb", ilica.addressLine())
        assertEquals("Avenija Dubrovnik 16, Zagreb", avenueMall.addressLine())
        assertEquals("10000 Zagreb", ilica.copy(address = null).addressLine())
        assertEquals("Ilica 12", ilica.copy(city = null, postalCode = null).addressLine())
    }

    @Test
    fun addressLine_isNullWithoutAnyAddressParts() {
        assertNull(ilica.copy(address = null, city = null, postalCode = null).addressLine())
    }
}
