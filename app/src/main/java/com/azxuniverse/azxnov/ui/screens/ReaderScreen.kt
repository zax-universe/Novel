package com.azxuniverse.azxnov.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.azxuniverse.azxnov.data.model.ChapterContent
import com.azxuniverse.azxnov.ui.navigation.Screen
import com.azxuniverse.azxnov.ui.viewmodel.NovelViewModel
import com.azxuniverse.azxnov.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(url: String, novelUrl: String, viewModel: NovelViewModel, navController: NavController) {
    val chapterState by viewModel.chapterState.collectAsState()

    LaunchedEffect(url) { viewModel.loadChapter(url, novelUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    (chapterState as? UiState.Success)?.data?.let {
                        Text(it.title, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = chapterState) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(state.message) { viewModel.loadChapter(url, novelUrl) }
                is UiState.Success -> ReaderContent(state.data, novelUrl, viewModel, navController)
            }
        }
    }
}

@Composable
fun ReaderContent(content: ChapterContent, novelUrl: String, viewModel: NovelViewModel, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            if (content.images.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(content.images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                    item { ChapterNavigation(content, novelUrl, navController) }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = content.contentText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 28.sp,
                            fontSize = 18.sp
                        ),
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    ChapterNavigation(content, novelUrl, navController)
                }
            }
        }
    }
}

@Composable
fun ChapterNavigation(content: ChapterContent, novelUrl: String, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = {
                content.navigation?.prev?.let {
                    navController.navigate(Screen.Reader(it, novelUrl)) {
                        popUpTo(Screen.Home) { inclusive = false }
                    }
                }
            },
            enabled = content.navigation?.prev != null,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Rounded.NavigateBefore, contentDescription = null)
            Text("Prev")
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = {
                content.navigation?.next?.let {
                    navController.navigate(Screen.Reader(it, novelUrl)) {
                        popUpTo(Screen.Home) { inclusive = false }
                    }
                }
            },
            enabled = content.navigation?.next != null,
            modifier = Modifier.weight(1f)
        ) {
            Text("Next")
            Icon(Icons.Rounded.NavigateNext, contentDescription = null)
        }
    }
}
