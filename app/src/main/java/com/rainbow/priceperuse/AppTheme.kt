package com.rainbow.priceperuse

import android.graphics.Color

data class AppPalette(val id: Int, val nameEn: String, val nameIt: String, val top: Int, val middle: Int, val bottom: Int, val edge: Int, val accent: Int, val selected: Int) {
    companion object {
        private fun color(hex: String) = Color.parseColor(hex)
        val all = listOf(
            AppPalette(0,"Lavender","Lavanda",color("#D6AEFA"),color("#EACBFB"),color("#D3A5FA"),color("#A966ED"),color("#42F5A5"),color("#99F5C4")),
            AppPalette(1,"Ocean","Oceano",color("#ADE2FA"),color("#DAF5FF"),color("#A3D8F5"),color("#5AA8D5"),color("#67DCEB"),color("#B6EFF7")),
            AppPalette(2,"Peach","Pesca",color("#FFD0B5"),color("#FFE9D7"),color("#FFC1A8"),color("#E8967E"),color("#FFB485"),color("#FFD9B5"))
        )
    }
}
