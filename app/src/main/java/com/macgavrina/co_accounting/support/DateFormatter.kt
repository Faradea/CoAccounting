package com.macgavrina.co_accounting.support

import android.icu.text.SimpleDateFormat
import android.os.Build
import java.text.ParseException

class DateFormatter() {

    //ToDo REFACT сделать singleton

    fun formatDateFromTimestamp(timestamp: Long): String {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val dv = java.lang.Long.valueOf(timestamp) // its need to be in milisecond
                val df = java.util.Date(dv)
                val vv = SimpleDateFormat("dd.MM.yyyy").format(df)
                return vv
            } else {
                TODO("VERSION.SDK_INT < N")
            }
        }

    fun getTimestampFromFormattedDate(formattedDate: String): Long? {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy")
        try {
            val mDate = sdf.parse(formattedDate)
            return mDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }
}