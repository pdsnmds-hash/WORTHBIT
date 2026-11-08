package com.rainbow.priceperuse

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class CollectionStatsTest {
    private fun item(id: String, cents: Long, uses: Int, name: String = id) = Item(id,name,cents,2,uses)
    @Test fun emptyCollectionHasNoRankings() {
        val stats=CollectionStats.from(emptyList())
        assertEquals(0,stats.itemCount);assertEquals(0L,stats.totalUses)
        assertEquals(0,stats.totalPurchasePrice.compareTo(BigDecimal.ZERO))
        assertTrue(stats.mostUsed.isEmpty());assertTrue(stats.bestValue.isEmpty());assertTrue(stats.highestPurchasePrice.isEmpty())
    }
    @Test fun summariesIncludeUnusedPurchasesButRankingsExcludeUnused() {
        val stats=CollectionStats.from(listOf(item("coffee",1200000,25),item("bag",12000,0),item("book",2000,10)))
        assertEquals(3,stats.itemCount);assertEquals(1,stats.unusedCount);assertEquals(35L,stats.totalUses)
        assertEquals(BigDecimal("12140.00"),stats.totalPurchasePrice)
        assertEquals(listOf("coffee","book"),stats.mostUsed.map {it.id})
        assertEquals(listOf("book","coffee"),stats.bestValue.map {it.id})
    }
    @Test fun graphIsTopFiveAndBestValueIsTopThree() {
        val stats=CollectionStats.from((1..12).map {item(it.toString(),it*100L,it)})
        assertEquals(listOf(12,11,10,9,8),stats.mostUsed.map {it.uses})
        assertEquals(5,stats.highestPurchasePrice.size);assertEquals(3,stats.bestValue.size)
    }
    @Test fun usesTotalDoesNotOverflowAnInteger() {
        assertEquals(4294967294L,CollectionStats.from(listOf(item("a",1,Int.MAX_VALUE),item("b",1,Int.MAX_VALUE))).totalUses)
    }
    @Test fun bestValueComparesUnroundedRatios() {
        val a=item("a",100,3);val b=item("b",333,10)
        assertEquals(a.costAt(),b.costAt())
        assertEquals("b",CollectionStats.from(listOf(a,b)).bestValue.first().id)
    }
    @Test fun changesAndUndoAreReflectedWithoutSeparateStoredStatistics() {
        val a=item("a",10000,0)
        assertEquals(1,CollectionStats.from(listOf(a)).unusedCount)
        a.uses++;assertEquals(1L,CollectionStats.from(listOf(a)).totalUses)
        a.uses--;assertTrue(CollectionStats.from(listOf(a)).bestValue.isEmpty())
        a.cents=5000;assertEquals(BigDecimal("50.00"),CollectionStats.from(listOf(a)).totalPurchasePrice)
        assertEquals(0,CollectionStats.from(emptyList()).itemCount)
    }
    @Test fun tiedRankingsHaveDeterministicOrder() {
        val stats=CollectionStats.from(listOf(item("b",100,1,"Zaino"),item("a",100,1,"Book")))
        assertEquals(listOf("a","b"),stats.mostUsed.map {it.id})
        assertEquals(listOf("a","b"),stats.bestValue.map {it.id})
    }
}
