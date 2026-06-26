package com.azxuniverse.azxnov.data.repository

import com.azxuniverse.azxnov.data.local.*
import com.azxuniverse.azxnov.data.model.*
import com.azxuniverse.azxnov.data.remote.NovelScraper
import kotlinx.coroutines.flow.Flow

class NovelRepository(
    private val scraper: NovelScraper,
    private val bookmarkDao: BookmarkDao,
    private val historyDao: HistoryDao
) {
    suspend fun getHome(): HomeData = scraper.getHome()
    suspend fun search(query: String): List<Novel> = scraper.search(query)
    suspend fun getDetail(url: String): Novel = scraper.getDetail(url)
    suspend fun getChapter(url: String): ChapterContent = scraper.getChapter(url)
    suspend fun getGenres(): List<Genre> = scraper.getGenres()

    val bookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
    val history: Flow<List<History>> = historyDao.getAllHistory()

    suspend fun addBookmark(novel: Novel) {
        bookmarkDao.insertBookmark(
            Bookmark(
                url = novel.url,
                title = novel.title,
                cover = novel.cover,
                type = novel.metadata?.type ?: novel.type,
                status = novel.metadata?.status ?: novel.status
            )
        )
    }

    suspend fun removeBookmark(url: String) {
        bookmarkDao.deleteBookmark(Bookmark(url, "", null, null, null))
    }

    fun isBookmarked(url: String): Flow<Boolean> = bookmarkDao.isBookmarked(url)

    suspend fun saveHistory(novel: Novel, chapter: ChapterContent, chapterUrl: String) {
        historyDao.insertHistory(
            History(
                novelUrl = novel.url,
                title = novel.title,
                cover = novel.cover,
                lastChapterTitle = chapter.title,
                lastChapterUrl = chapterUrl
            )
        )
    }
}
