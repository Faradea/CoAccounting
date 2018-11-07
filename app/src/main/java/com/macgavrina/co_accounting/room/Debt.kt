package com.macgavrina.co_accounting.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
class Debt {

    //ToDo Использовать не только String как тип данных
    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "sender")
    public var sender: String? = null
        set
        get

    @ColumnInfo(name = "receiver")
    public var receiver: String? = null
        set
        get

    @ColumnInfo(name = "amount")
    public var amount: String? = null
        set
        get

    @ColumnInfo(name = "datetime")
    public var datetime: String? = null
        set
        get

    @ColumnInfo(name = "comment")
    public var comment: String? = null
        set
        get
}