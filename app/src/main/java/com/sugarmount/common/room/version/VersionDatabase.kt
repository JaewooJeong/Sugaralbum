package com.sugarmount.common.room.version

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VersionT::class], version = 1)
abstract class VersionDatabase: RoomDatabase() {
    abstract fun versionDao(): VersionDao

    companion object {
        private var instance: VersionDatabase? = null

        fun getInstance(context: Context): VersionDatabase? {
            if(instance == null){
                synchronized(VersionDatabase::class){
                    instance = Room.databaseBuilder(context.applicationContext, VersionDatabase::class.java, VersionDatabase::class.java.name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return instance
        }
    }
}