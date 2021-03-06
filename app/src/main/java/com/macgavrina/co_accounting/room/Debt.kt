package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.macgavrina.co_accounting.support.STATUS_ACTIVE

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
    var status: Int = STATUS_ACTIVE

    @ColumnInfo(name = "currencyId")
    var currencyId: Int = -1

    var currencySymbol: String = ""

    @ColumnInfo(name = "expertModeIsEnabled")
    var expertModeIsEnabled: Boolean = false

    @ColumnInfo(name = "isCorrect")
    var isCorrect: Boolean = false

    @ColumnInfo(name = "creatorUserId")
    var creatorUserId: String? = null

    var senderId: Int = -1

    @ColumnInfo(name = "externalId")
    var externalId: Int? = null

    override fun toString(): String {
        return "uid = $uid, tripId = $tripId, spentAmount = $spentAmount, datetime = $datetime, comment = $comment, status = $status, currencyId = $currencyId, expertModeIsEnabled = $expertModeIsEnabled, isCorrect = $isCorrect, senderId = $senderId"
    }
}