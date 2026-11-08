package com.rainbow.priceperuse

import org.junit.Assert.*
import org.junit.Test

class UiSizingTest {
    @Test fun nativeTextReducesForNarrowScreens() {
        assertEquals(1f,UiSizing.compactScale(411f),0f)
        assertEquals(1f,UiSizing.compactScale(380f),0f)
        assertEquals(320f/380f,UiSizing.compactScale(320f),.0001f)
        assertTrue(UiSizing.compactScale(320f)<UiSizing.compactScale(380f))
    }
}
