package com.sugarmount.common.room.version

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sugarmount.common.model.MvConfig

@Entity(tableName = "VersionT")
class VersionT{
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @ColumnInfo(name = "infoVersion")
    var infoVersion: String = ""

    constructor(infoVersion: String) {
        this.infoVersion = infoVersion
    }


}