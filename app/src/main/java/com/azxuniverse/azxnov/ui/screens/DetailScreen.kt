package com.azxuniverse.azxnov.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.azxuniverse.azxnov.data.model.ChapterLink
import com.azxuniverse.azxnov.data.model.Novel
import com.azxuniverse.azxnov.ui.navigation.Screen
import com.azxuniverse.azxnov.ui.viewmodel.NovelViewModel
import com.azxuniverse.azxnov.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(url: String, viewModel: NovelViewModel, navController: NavController) {
    val detailState by viewModel.detailState.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    LaunchedEffect(url) { viewModel.loadDetail(url) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    (detailState as? UiState.Success)?.data?.let { novel ->
                        val isBookmarked = bookmarks.any { it.url == novel.url }
                        IconButton(onClick = { viewModel.toggleBookmark(novel) }) {
                            Icon(
                                if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = detailState) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(state.message) { viewModel.loadDetail(url) }
                is UiState.Success -> DetailContent(state.data, navController)
            }
        }
    }
}

@Composable
fun DetailContent(novel: Novel, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { NovelHeader(novel) }
        item { NovelInfo(novel) }
        item {
            Text(
                "Synopsis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = novel.synopsis ?: "No synopsis available.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
        item {
            Text(
                "Chapters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
        items(novel.chapters) { chapter ->
            ChapterItem(chapter) {
                navController.navigate(Screen.Reader(chapter.url, novel.url))
            }
        }
    }
}

@Composable
fun NovelHeader(novel: Novel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = novel.cover,
            contentDescription = novel.title,
            modifier = Modifier
                .size(120.dp, 160.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = novel.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            novel.metadata?.let { meta ->
                Spacer(modifier = Modifier.height(8.dp))
                StatusChip(meta.status ?: "Unknown")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Author: ${meta.author ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                Text("Type: ${meta.type ?: "Unknown"}", style = MaterialTheme.typography.bodySmall)
                Text("Rating: ⭐ ${meta.rating ?: "0"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovelInfo(novel: Novel) {
    FlowRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        novel.genres.forEach { genre ->
            AssistChip(onClick = {}, label = { Text(genre) })
        }
    }
}

@Composable
fun ChapterItem(chapter: ChapterLink, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = chapter.release ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp), thickness = 0.5.dp)
    }
}
