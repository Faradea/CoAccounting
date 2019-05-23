package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = arrayOf(ForeignKey(entity = Debt::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("debtId"),
        onDelete = ForeignKey.CASCADE)))

class Expense {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "debtId")
    public var debtId: Int? = null
        set
        get

    //ToDo Delete (use comment)
    @ColumnInfo(name = "expenseName")
    public var expenseName: String? = null
        set
        get

    public var receiversList: String? = null
        set
        get

    @ColumnInfo(name = "totalAmount")
    public var totalAmount: String? = null
        set
        get

    @ColumnInfo(name = "comment")
    public var comment: String = ""
        set
        get

    override fun toString(): String {
        return "debtId = $debtId, expenseName = $expenseName, receiversList = $receiversList, totalAmount = $totalAmount"
    }
}