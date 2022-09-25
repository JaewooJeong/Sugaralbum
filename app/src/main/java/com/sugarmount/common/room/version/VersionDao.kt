package com.sugarmount.common.room.version

import androidx.room.Dao
import androidx.room.Query
import com.sugarmount.common.room.BaseDao

@Dao
abstract class VersionDao: BaseDao<VersionT> {

    @Query("SELECT * FROM VersionT LIMIT 1")
    abstract fun getVersion(): List<VersionT>

}