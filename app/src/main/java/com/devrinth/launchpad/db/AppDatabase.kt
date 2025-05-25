package com.devrinth.launchpad.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CachedApp::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedAppDao(): CachedAppDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "launcher_app_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
