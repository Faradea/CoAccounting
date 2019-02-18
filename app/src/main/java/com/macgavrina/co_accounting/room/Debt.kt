package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Debt {

    @PrimaryKey(autoGenerate = true)
    public var uid: Int = 0
        set
        get

    @ColumnInfo(name = "tripId")
    public var tripId: Int? = null
        set
        get

    //ToDo REFACT add to indexes
    @ColumnInfo(name = "senderId")
    public var senderId: String? = null
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

    @ColumnInfo(name = "status")
    public var status: String? = null
        set
        get

    //For future functionality: to group debts in events
    @ColumnInfo(name = "eventId")
    public var eventId: String? = null
        set
        get

    override fun toString(): String {
        return "uid = $uid, tripId = $tripId, senderId = $senderId, spentAmount = $spentAmount, datetime = $datetime, comment = $comment, status = $status"
    }
}