package com.sugarmount.common.room.info

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "InfoT")
class InfoT{
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    @ColumnInfo(name = "locale")
    var locale: String = "locale"

    @ColumnInfo(name = "type")
    var type: String = "type"

    @ColumnInfo(name = "time")
    var time: String = "time"

    @ColumnInfo(name = "title")
    var title: String = "title"

    @ColumnInfo(name = "content")
    var content: String = "content"

    @ColumnInfo(name = "url")
    var url: String = "url"

    constructor(
        locale: String,
        type: String,
        time: String,
        title: String,
        content: String,
        url: String
    ) {
        this.locale = locale
        this.type = type
        this.time = time
        this.title = title
        this.content = content
        this.url = url
    }
}