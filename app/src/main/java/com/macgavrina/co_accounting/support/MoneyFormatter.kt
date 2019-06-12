package com.macgavrina.co_accounting.support

import android.icu.text.DecimalFormat
import android.os.Build

object MoneyFormatter {

    fun formatAmountForEditableText(amount: Double): String {
        if (amount == 0.0) return ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val format = DecimalFormat("0.##")
            return format.format(amount)
        } else {
            return amount.toString()
        }
    }

    fun formatAmountForReadOnlyText(amount: Double): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val format = DecimalFormat("0.##")
            return format.format(amount)
        } else {
            return amount.toString()
        }
    }
}