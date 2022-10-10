package com.sugarmount.common.room

import androidx.room.*

@Dao
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(value: T)

    @Insert
    fun insert(vararg obj: T)

    @Update
    fun update(value: T)

    @Delete
    fun delete(value: T)
}