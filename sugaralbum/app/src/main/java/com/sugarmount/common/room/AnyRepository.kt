package com.sugarmount.common.room

import android.app.Application
import com.sugarmount.common.room.info.InfoDao
import com.sugarmount.common.room.info.InfoDatabase
import com.sugarmount.common.room.info.InfoT
import com.sugarmount.common.room.version.VersionDao
import com.sugarmount.common.room.version.VersionDatabase
import com.sugarmount.common.room.version.VersionT

class AnyRepository(application: Application) {
    private val versionDatabase = VersionDatabase.getInstance(application)!!
    private val versionDao: VersionDao = versionDatabase.versionDao()

    private val infoDatabase = InfoDatabase.getInstance(application)!!
    private val infoDao: InfoDao = infoDatabase.infoDao()

    fun getVersion(): List<VersionT> {
        return versionDao.getVersion()
    }

    fun getInfo(locale: String, type: String): List<InfoT> {
        return infoDao.getInfo(locale, type)
    }

    fun insert(data: VersionT){
        try{
            Thread {
                versionDao.insert(data)
            }.start()
        }catch (e: Exception){
        }
    }

    fun insert(data: InfoT){
        try{
            Thread {
                infoDao.insert(data)
            }.start()
        }catch (e: Exception){
        }
    }

}