package com.azxuniverse.azxnov.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.azxuniverse.azxnov.data.local.AppDatabase
import com.azxuniverse.azxnov.data.remote.NovelScraper
import com.azxuniverse.azxnov.data.repository.NovelRepository

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val database by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "azxnov.db").build()
    }

    private val scraper by lazy { NovelScraper() }

    private val repository by lazy {
        NovelRepository(
            scraper = scraper,
            bookmarkDao = database.bookmarkDao(),
            historyDao = database.historyDao()
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NovelViewModel::class.java)) {
            return NovelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
