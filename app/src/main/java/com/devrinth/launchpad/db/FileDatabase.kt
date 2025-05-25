package com.devrinth.launchpad.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FileEntity::class], version = 1)
abstract class FileDatabase : RoomDatabase() {
    abstract fun cachedFiles(): FileDao

    companion object {
        @Volatile private var INSTANCE: FileDatabase? = null

        fun getInstance(context: Context): FileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FileDatabase::class.java,
                    "file_list_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
