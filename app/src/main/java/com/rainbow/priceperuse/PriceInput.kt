package com.rainbow.priceperuse

import java.math.BigDecimal

object PriceInput {
    fun cents(raw: String): Long? = try {
        val clean = raw.trim().replace(" ", "").replace(",", ".")
        if (!clean.matches(Regex("[0-9]{1,9}(\\.[0-9]{1,2})?"))) null
        else BigDecimal(clean).movePointRight(2).longValueExact().takeIf { it > 0 }
    } catch (_: Exception) { null }
}
