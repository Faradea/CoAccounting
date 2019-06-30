package com.macgavrina.co_accounting.room

import androidx.room.*
import com.macgavrina.co_accounting.support.STATUS_ACTIVE
import kotlin.math.exp


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

    @ColumnInfo(name = "externalId")
    var externalId: Int? = null

    @ColumnInfo(name = "status")
    var status: Int = STATUS_ACTIVE

    override fun toString(): String {
        return "ReceiverWithAmount: uid = $uid, contactId = $contactId, amount = $amount, expenseId = $expenseId, debtId = $debtId"
    }
}