package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.macgavrina.co_accounting.support.STATUS_ACTIVE

@Entity(foreignKeys = arrayOf(ForeignKey(entity = Debt::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("debtId"),
        onDelete = ForeignKey.CASCADE)))

class Expense {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "debtId")
    var debtId: Int = -1

    @ColumnInfo(name = "totalAmount")
    var totalAmount: Double = 0.0

    @ColumnInfo(name = "comment")
    var comment: String = ""

    @ColumnInfo(name = "isForExpertMode")
    var isForExpertMode: Boolean = false

    var receiversList: String = ""

    @ColumnInfo(name = "creatorUserId")
    var creatorUserId: String? = null

    @ColumnInfo(name = "externalId")
    var externalId: Int? = null

    @ColumnInfo(name = "status")
    var status: Int = STATUS_ACTIVE

    override fun toString(): String {
        return "debtId = $debtId, comment = $comment, totalAmount = $totalAmount, isForExpertMode = $isForExpertMode"
    }
}