package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Debt {

    //ToDo Использовать не только String как тип данных
    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "receiverId")
    public var receiverId: String? = null
        set
        get

    @ColumnInfo(name = "spentAmount")
    public var spentAmount: String? = null
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