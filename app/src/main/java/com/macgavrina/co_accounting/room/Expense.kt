package com.macgavrina.co_accounting.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey



@Entity
class Expense {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "expenseName")
    public var expenseName: String? = null
        set
        get

    @ColumnInfo(name = "eventId")
    public var eventId: String? = null
        set
        get

    @ColumnInfo(name = "receiversList")
    public var receiversList: String? = null
        set
        get

    @ColumnInfo(name = "totalAmount")
    public var totalAmount: String? = null
        set
        get
}