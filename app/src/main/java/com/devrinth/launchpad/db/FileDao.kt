package com.devrinth.launchpad.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<FileEntity>)

    @Query("SELECT * FROM files WHERE name LIKE '%' || :query || '%'")
    suspend fun searchFiles(query: String): List<FileEntity>

    @Query("DELETE FROM files")
    suspend fun clearFiles()
}
