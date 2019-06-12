package com.macgavrina.co_accounting.support

import android.icu.text.SimpleDateFormat
import android.os.Build
import com.macgavrina.co_accounting.logging.Log
import java.text.ParseException
import android.text.format.DateUtils
import com.macgavrina.co_accounting.MainApplication


class DateFormatter() {

    fun formatDateFromTimestamp(timestamp: Long): String {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val vv = SimpleDateFormat("dd.MM.yyyy").format(timestamp)
                return vv
            } else {
                TODO("VERSION.SDK_INT < N")
            }

//        val date = DateUtils.formatDateTime(MainApplication.applicationContext(), timestamp, DateUtils.FORMAT_SHOW_DATE)
//        Log.d("format timestamp, date = $date")
//        return date

        }

    fun formatTimeFromTimestamp(timestamp: Long): String {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val vv = SimpleDateFormat("HH:mm").format(timestamp)
            Log.d("formatting time from timestamp, timestamp = $timestamp, time = $vv")
            return vv
        } else {
            TODO("VERSION.SDK_INT < N")
        }

//            val time = DateUtils.formatDateTime(MainApplication.applicationContext(), timestamp, DateUtils.FORMAT_SHOW_TIME)
//            Log.d("format timestamp, time = $time")
//            return time

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

    fun getTimestampFromFormattedDateTime(formattedDateTime: String): Long? {
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
        try {
            val mDate = sdf.parse(formattedDateTime)
            return mDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }
}