package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
class SenderWithAmount {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "contactId")
    var contactId: Int = -1

    @ColumnInfo(name = "amount")
    var amount: Double = 0.0

    @ColumnInfo(name = "debtId")
    var debtId: Int = -1

    @ColumnInfo(name = "externalId")
    var externalId: Int? = null

    override fun toString(): String {
        return "SenderWithAmount: contactId = $contactId, amount = $amount, debtId = $debtId"
    }
}