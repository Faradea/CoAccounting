package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
//@TypeConverters(ListTypeConverter::class)
class Trip {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "title")
    var title: String = ""

    @ColumnInfo(name = "startdate")
    var startdate: Long = 0

    @ColumnInfo(name = "enddate")
    var enddate: Long = 0

    @ColumnInfo(name = "isCurrent")
    var isCurrent: Boolean = false

    @ColumnInfo(name = "status")
    var status: String = "active"

    @ColumnInfo(name = "lastUsedCurrencyId")
    var lastUsedCurrencyId: Int = -1


    override fun toString(): String {
        return "uid = $uid, title = $title, startdate = $startdate, enddate = $enddate, isCurrent = $isCurrent, status = $status"
    }
}