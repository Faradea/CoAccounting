package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
//@TypeConverters(ListTypeConverter::class)
class Trip {
    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "title")
    public var title: String? = null
        set
        get

    @ColumnInfo(name = "startdate")
    public var startdate: Long? = null
        set
        get

    @ColumnInfo(name = "enddate")
    public var enddate: Long? = null
        set
        get

    @ColumnInfo(name = "isCurrent")
    public var isCurrent: Boolean = false
        set
        get

    @ColumnInfo(name = "status")
    public var status: String? = "active"
        set
        get
}