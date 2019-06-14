package com.macgavrina.co_accounting.room

import androidx.room.*


@Entity(foreignKeys = arrayOf(ForeignKey(entity = Expense::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("expenseId"),
        onDelete = ForeignKey.CASCADE)))

class ReceiverWithAmountForDB {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get


    //ToDo REFACT Add to indexes
    @ColumnInfo(name = "contactId")
    public var contactId: Int = -1
        set
        get

    @ColumnInfo(name = "amount")
    public var amount: Double = 0.0
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