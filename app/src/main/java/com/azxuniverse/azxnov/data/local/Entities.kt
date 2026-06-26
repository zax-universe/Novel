package com.azxuniverse.azxnov.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "bookmarks")
@Serializable
data class Bookmark(
    @PrimaryKey val url: String,
    val title: String,
    val cover: String?,
    val type: String?,
    val status: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
@Serializable
data class History(
    @PrimaryKey val novelUrl: String,
    val title: String,
    val cover: String?,
    val lastChapterTitle: String,
    val lastChapterUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
