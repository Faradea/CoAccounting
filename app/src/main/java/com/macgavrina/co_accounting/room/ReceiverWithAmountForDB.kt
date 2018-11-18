package com.macgavrina.co_accounting.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
/*
        @Entity(foreignKeys = arrayOf(ForeignKey(entity = Expense::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("expenseId"),
        onDelete = ForeignKey.CASCADE)))*/

@Entity
class ReceiverWithAmountForDB {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "contactId")
    public var contactId: String? = null
        set
        get

    @ColumnInfo(name = "amount")
    public var amount: String? = null
        set
        get

    @ColumnInfo(name = "expenseId")
    public var expenseId: String? = null
        set
        get

    @ColumnInfo(name = "debtId")
    public var debtId: String? = null
        set
        get
}