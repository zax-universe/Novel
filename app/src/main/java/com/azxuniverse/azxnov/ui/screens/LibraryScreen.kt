package com.azxuniverse.azxnov.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.azxuniverse.azxnov.data.local.Bookmark
import com.azxuniverse.azxnov.data.local.History
import com.azxuniverse.azxnov.data.model.ChapterLink
import com.azxuniverse.azxnov.data.model.Novel
import com.azxuniverse.azxnov.ui.navigation.Screen
import com.azxuniverse.azxnov.ui.viewmodel.NovelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: NovelViewModel, navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("My Library", fontWeight = FontWeight.Bold) })
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    listOf("Bookmarks", "History").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> BookmarkList(bookmarks, navController)
                1 -> HistoryList(history, navController)
            }
        }
    }
}

@Composable
fun BookmarkList(bookmarks: List<Bookmark>, navController: NavController) {
    if (bookmarks.isEmpty()) {
        EmptyState("Your library is empty")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(bookmarks) { bookmark ->
                LatestNovelItem(
                    Novel(
                        title = bookmark.title,
                        url = bookmark.url,
                        cover = bookmark.cover,
                        type = bookmark.type,
                        status = bookmark.status
                    )
                ) { navController.navigate(Screen.Detail(bookmark.url)) }
            }
        }
    }
}

@Composable
fun HistoryList(history: List<History>, navController: NavController) {
    if (history.isEmpty()) {
        EmptyState("No reading history yet")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(history) { item ->
                LatestNovelItem(
                    Novel(
                        title = item.title,
                        url = item.novelUrl,
                        cover = item.cover,
                        latestChapter = ChapterLink(item.lastChapterTitle, item.lastChapterUrl)
                    )
                ) { navController.navigate(Screen.Detail(item.novelUrl)) }
            }
        }
    }
}
