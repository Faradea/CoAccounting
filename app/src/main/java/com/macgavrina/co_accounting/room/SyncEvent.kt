package com.macgavrina.co_accounting.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SyncEvent {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "entityType")
    var entityType: String? = null

    @ColumnInfo(name = "entityInternalId")
    var entityInternalId: Int? = null

    @ColumnInfo(name = "entityExternalId")
    var entityExternalId: Int? = null

    @ColumnInfo(name = "fieldName")
    var fieldName: String? = null

    @ColumnInfo(name = "fieldValue")
    var fieldValue: String? = null

    @ColumnInfo(name = "isSyncedWithServer")
    var isSyncedWithServer: Boolean = false

    @ColumnInfo(name = "timestamp")
    var timestamp: Long = 0

}