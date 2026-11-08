package com.rainbow.priceperuse

import java.math.BigDecimal
import java.math.RoundingMode

data class Item(val id: String, var name: String, var cents: Long, var icon: Int, var uses: Int = 0) {
    fun costAt(count: Int = uses): BigDecimal? = if (count <= 0) null else BigDecimal.valueOf(cents, 2).divide(BigDecimal(count), 2, RoundingMode.HALF_UP)
}
