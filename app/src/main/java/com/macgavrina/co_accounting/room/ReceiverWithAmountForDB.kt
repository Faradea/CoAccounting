package com.macgavrina.co_accounting.room

import androidx.room.*


@Entity(foreignKeys = arrayOf(ForeignKey(entity = Expense::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("expenseId"),
        onDelete = ForeignKey.CASCADE)))

class ReceiverWithAmountForDB {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "contactId")
    var contactId: Int = -1

    @ColumnInfo(name = "amount")
    var amount: Double = 0.0

    @ColumnInfo(name = "expenseId")
    var expenseId: Int = -1

    @ColumnInfo(name = "debtId")
    var debtId: Int = -1
}