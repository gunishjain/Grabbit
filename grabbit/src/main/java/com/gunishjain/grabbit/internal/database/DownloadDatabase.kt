package com.gunishjain.grabbit.internal.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadEntity::class], version = 1)
abstract class DownloadDatabase : RoomDatabase(){
    abstract fun downloadDao() : DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        fun getInstance(context: Context): DownloadDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DownloadDatabase::class.java,
                    "downloads.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}