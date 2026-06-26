package com.azxuniverse.azxnov.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Search : Screen
    @Serializable data object Library : Screen
    @Serializable data object About : Screen
    @Serializable data class Detail(val url: String) : Screen
    @Serializable data class Reader(val url: String, val novelUrl: String) : Screen
}
