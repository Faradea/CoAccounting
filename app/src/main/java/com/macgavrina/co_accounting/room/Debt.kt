package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Debt {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "tripId")
    var tripId: Int = -1

    @ColumnInfo(name = "spentAmount")
    var spentAmount: Double = 0.0

    @ColumnInfo(name = "datetime")
    var datetime: Long = 0

    @ColumnInfo(name = "comment")
    var comment: String = ""

    @ColumnInfo(name = "status")
    var status: String = "active"

    @ColumnInfo(name = "currencyId")
    var currencyId: Int = -1

    var currencySymbol: String = ""

    @ColumnInfo(name = "expertModeIsEnabled")
    var expertModeIsEnabled: Boolean = false

    @ColumnInfo(name = "isCorrect")
    var isCorrect: Boolean = false

    override fun toString(): String {
        return "uid = $uid, tripId = $tripId, spentAmount = $spentAmount, datetime = $datetime, comment = $comment, status = $status, currencyId = $currencyId, expertModeIsEnabled = $expertModeIsEnabled, isCorrect = $isCorrect"
    }
}