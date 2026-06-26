package com.azxuniverse.azxnov.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Novel(
    val title: String,
    val url: String,
    val cover: String? = null,
    val type: String? = null,
    val status: String? = null,
    val latestChapter: ChapterLink? = null,
    val synopsis: String? = null,
    val metadata: NovelMetadata? = null,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val chapters: List<ChapterLink> = emptyList()
)

@Serializable
data class ChapterLink(
    val title: String,
    val url: String,
    val release: String? = null
)

@Serializable
data class NovelMetadata(
    val type: String? = null,
    val status: String? = null,
    val rating: String? = null,
    val bookmarks: String? = null,
    val country: String? = null,
    val published: String? = null,
    val author: String? = null,
    val lastUpdated: String? = null
)

@Serializable
data class ChapterContent(
    val title: String,
    val fullTitle: String,
    val contentText: String,
    val images: List<String> = emptyList(),
    val navigation: Navigation? = null
)

@Serializable
data class Navigation(
    val prev: String? = null,
    val toc: String? = null,
    val next: String? = null
)

@Serializable
data class HomeData(
    val popular: List<Novel> = emptyList(),
    val latest: List<Novel> = emptyList()
)

@Serializable
data class Genre(
    val name: String,
    val count: String,
    val url: String
)
