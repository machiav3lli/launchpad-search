package com.devrinth.launchpad.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_apps")
data class CachedApp(
    @PrimaryKey val packageName: String,
    val appName: String,
    val icon: ByteArray
)
