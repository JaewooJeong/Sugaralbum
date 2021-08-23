package com.sugarmount.common.room.info

import androidx.room.Dao
import androidx.room.Query
import com.sugarmount.common.room.BaseDao

@Dao
abstract class InfoDao: BaseDao<InfoT> {

    @Query("SELECT * FROM InfoT WHERE locale = :locale AND type = :type ORDER BY time DESC;")
    abstract fun getInfo(locale: String, type: String): List<InfoT>
}