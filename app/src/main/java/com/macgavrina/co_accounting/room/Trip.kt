package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.macgavrina.co_accounting.support.STATUS_ACTIVE

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
    var status: Int = STATUS_ACTIVE

    @ColumnInfo(name = "lastUsedCurrencyId")
    var lastUsedCurrencyId: Int = -1

    @ColumnInfo(name = "creatorUserId")
    var creatorUserId: String? = null

    @ColumnInfo(name = "externalId")
    var externalId: Int? = null


    override fun toString(): String {
        return "uid = $uid, title = $title, startdate = $startdate, enddate = $enddate, isCurrent = $isCurrent, status = $status"
    }
}