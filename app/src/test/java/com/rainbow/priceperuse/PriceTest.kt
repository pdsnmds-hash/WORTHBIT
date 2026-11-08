package com.rainbow.priceperuse

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class PriceTest {
    @Test fun beforeFirstUseHasNoCost() { assertNull(Item("1","Coffee",1200000,0).costAt()) }
    @Test fun referenceCalculationAndPreviousCost() {
        val item=Item("1","Coffee",1200000,0,25)
        assertEquals(BigDecimal("480.00"),item.costAt())
        assertEquals(BigDecimal("500.00"),item.costAt(24))
    }
    @Test fun roundsToNearestCent() { assertEquals(BigDecimal("3.33"),Item("1","Box",1000,2,3).costAt()) }
    @Test fun acceptsBothDecimalSeparators() {
        assertEquals(123456L,PriceInput.cents("1234,56"));assertEquals(123456L,PriceInput.cents("1234.56"))
        assertEquals(1200000L,PriceInput.cents("12 000"))
    }
    @Test fun rejectsInvalidPrices() { listOf("","0","-1","1.234","abc","1,2.3","1000000000").forEach { assertNull(it,PriceInput.cents(it)) } }
    @Test fun undoRestoresPreviousCost() {
        val item=Item("1","Coffee",1200000,0,24);val previous=item.uses
        item.uses++;assertEquals(BigDecimal("480.00"),item.costAt());item.uses=previous
        assertEquals(BigDecimal("500.00"),item.costAt())
    }
}
