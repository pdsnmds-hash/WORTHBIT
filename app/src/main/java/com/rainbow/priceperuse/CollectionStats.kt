package com.rainbow.priceperuse

import java.math.BigDecimal
import java.math.BigInteger

/** Computed from the current inventory; no duplicated counters or synthetic history. */
data class CollectionStats(
    val itemCount: Int,
    val totalPurchasePrice: BigDecimal,
    val totalUses: Long,
    val unusedCount: Int,
    val mostUsed: List<Item>,
    val highestPurchasePrice: List<Item>,
    val bestValue: List<Item>
) {
    companion object {
        fun from(items: List<Item>): CollectionStats {
            val stableOrder = compareBy<Item> { it.name.lowercase(java.util.Locale.ROOT) }.thenBy { it.id }
            // Compare exact ratios, so equal rounded display prices do not hide the real winner.
            val costOrder = Comparator<Item> { a, b ->
                BigInteger.valueOf(a.cents).multiply(BigInteger.valueOf(b.uses.toLong()))
                    .compareTo(BigInteger.valueOf(b.cents).multiply(BigInteger.valueOf(a.uses.toLong())))
            }.then(stableOrder)
            return CollectionStats(
                itemCount = items.size,
                totalPurchasePrice = items.fold(BigDecimal.ZERO) { total, item -> total + BigDecimal.valueOf(item.cents, 2) },
                totalUses = items.sumOf { it.uses.toLong() },
                unusedCount = items.count { it.uses == 0 },
                mostUsed = items.filter { it.uses > 0 }.sortedWith(compareByDescending<Item> { it.uses }.then(stableOrder)).take(5),
                highestPurchasePrice = items.sortedWith(compareByDescending<Item> { it.cents }.then(stableOrder)).take(5),
                bestValue = items.filter { it.uses > 0 }.sortedWith(costOrder).take(3)
            )
        }
    }
}
