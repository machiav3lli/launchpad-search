package com.devrinth.launchpad.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedAppDao {

    @Query("SELECT * FROM cached_apps WHERE appName LIKE :query OR packageName LIKE :query ORDER BY appName ASC")
    suspend fun searchApps(query: String): List<CachedApp>

    @Query("SELECT * FROM cached_apps")
    suspend fun getAllApps(): List<CachedApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<CachedApp>)

    @Query("DELETE FROM cached_apps")
    suspend fun clearAll()
}
