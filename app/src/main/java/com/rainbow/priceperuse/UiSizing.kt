package com.rainbow.priceperuse

object UiSizing {
    fun compactScale(widthDp:Float):Float = (widthDp/380f).coerceIn(.75f,1f)
}
