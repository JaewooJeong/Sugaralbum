package com.sugarmount.common.room.info

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [InfoT::class], version = 1)
abstract class InfoDatabase: RoomDatabase() {
    abstract fun infoDao(): InfoDao

    companion object {
        private var instance: InfoDatabase? = null

        fun getInstance(context: Context): InfoDatabase? {
            if(instance == null){
                synchronized(InfoDatabase::class){
                    instance = Room.databaseBuilder(context.applicationContext, InfoDatabase::class.java, InfoDatabase::class.java.name)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return instance
        }
    }
}