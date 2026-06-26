package com.azxuniverse.azxnov.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azxuniverse.azxnov.data.local.Bookmark
import com.azxuniverse.azxnov.data.local.History
import com.azxuniverse.azxnov.data.model.*
import com.azxuniverse.azxnov.data.repository.NovelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NovelViewModel(private val repository: NovelRepository) : ViewModel() {

    private val _homeState = MutableStateFlow<UiState<HomeData>>(UiState.Loading)
    val homeState: StateFlow<UiState<HomeData>> = _homeState.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<Novel>>>(UiState.Success(emptyList()))
    val searchState: StateFlow<UiState<List<Novel>>> = _searchState.asStateFlow()

    private val _detailState = MutableStateFlow<UiState<Novel>>(UiState.Loading)
    val detailState: StateFlow<UiState<Novel>> = _detailState.asStateFlow()

    private val _chapterState = MutableStateFlow<UiState<ChapterContent>>(UiState.Loading)
    val chapterState: StateFlow<UiState<ChapterContent>> = _chapterState.asStateFlow()

    val bookmarks: StateFlow<List<Bookmark>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<History>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { loadHome() }

    fun loadHome() {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            _homeState.value = runCatching { UiState.Success(repository.getHome()) }
                .getOrElse { UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            _searchState.value = runCatching { UiState.Success(repository.search(query)) }
                .getOrElse { UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun loadDetail(url: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            _detailState.value = runCatching { UiState.Success(repository.getDetail(url)) }
                .getOrElse { UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun loadChapter(url: String, novelUrl: String) {
        viewModelScope.launch {
            _chapterState.value = UiState.Loading
            runCatching { repository.getChapter(url) }
                .onSuccess { chapter ->
                    _chapterState.value = UiState.Success(chapter)
                    (detailState.value as? UiState.Success)?.data?.let { novel ->
                        repository.saveHistory(novel, chapter, url)
                    }
                }
                .onFailure { _chapterState.value = UiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun toggleBookmark(novel: Novel) {
        viewModelScope.launch {
            if (bookmarks.value.any { it.url == novel.url }) {
                repository.removeBookmark(novel.url)
            } else {
                repository.addBookmark(novel)
            }
        }
    }

    fun isBookmarked(url: String): Flow<Boolean> = repository.isBookmarked(url)
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
