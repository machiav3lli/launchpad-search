package com.devrinth.launchpad.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey val path: String,
    val name: String,
    val mimeType: String
)
