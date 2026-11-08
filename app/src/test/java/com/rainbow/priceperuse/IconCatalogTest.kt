package com.rainbow.priceperuse

import org.junit.Assert.*
import org.junit.Test

class IconCatalogTest {
    @Test fun catalogHasThirtyDistinctLocalizedEntries() {
        assertEquals(30,ItemIcons.count)
        assertEquals(ItemIcons.count,ItemIcons.namesIt.size)
        assertEquals(30,ItemIcons.namesEn.toSet().size)
        assertEquals(30,ItemIcons.namesIt.toSet().size)
        assertTrue(ItemIcons.namesIt.all {it.isNotBlank()})
    }
    @Test fun originalIdentifiersArePreserved() {
        assertEquals("Coffee machine",ItemIcons.name(0,false))
        assertEquals("Backpack",ItemIcons.name(1,false))
        assertEquals("Box",ItemIcons.name(2,false))
        assertEquals("Glasses",ItemIcons.name(29,false))
        assertEquals("Occhiali",ItemIcons.name(29,true))
    }
}
