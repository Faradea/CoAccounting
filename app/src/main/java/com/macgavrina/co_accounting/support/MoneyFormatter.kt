package com.macgavrina.co_accounting.support

import android.icu.text.DecimalFormat
import android.os.Build

object MoneyFormatter {

    fun formatAmount(amount: Double): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val format = DecimalFormat("0.#")
            return format.format(amount)
        } else {
            return amount.toString()
        }
    }
}