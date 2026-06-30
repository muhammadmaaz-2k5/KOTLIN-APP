package com.job2day.nazaarabox.navigation

import androidx.navigation.NavController
import com.job2day.nazaarabox.core.HomeCategory
import com.job2day.nazaarabox.core.MediaItem
import com.job2day.nazaarabox.core.ThemedSection
import com.job2day.nazaarabox.routes.AppRoutes
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val MEDIA_ITEM_KEY = "mediaItem"
private const val ITEMS_KEY = "items"
private const val TITLE_KEY = "title"
private const val CATEGORY_KEY = "category"
private const val THEMED_SECTION_KEY = "themedSection"

fun NavController.navigateToDetail(item: MediaItem) {
    currentBackStackEntry?.savedStateHandle?.set(MEDIA_ITEM_KEY, AppRoutes.encodeItem(item))
    navigate(AppRoutes.DETAIL)
}

fun NavController.navigateToPlayer(item: MediaItem) {
    currentBackStackEntry?.savedStateHandle?.set(MEDIA_ITEM_KEY, AppRoutes.encodeItem(item))
    navigate(AppRoutes.PLAYER)
}

fun NavController.navigateToActor(personId: Int) {
    navigate(AppRoutes.actor(personId))
}

fun NavController.navigateToSeason(item: MediaItem, seasonNumber: Int, seasonName: String) {
    currentBackStackEntry?.savedStateHandle?.set(MEDIA_ITEM_KEY, AppRoutes.encodeItem(item))
    currentBackStackEntry?.savedStateHandle?.set("seasonNumber", seasonNumber)
    currentBackStackEntry?.savedStateHandle?.set("seasonName", seasonName)
    navigate(AppRoutes.SEASON)
}

fun NavController.navigateToSeeAll(title: String, items: List<MediaItem>) {
    currentBackStackEntry?.savedStateHandle?.set(TITLE_KEY, title)
    currentBackStackEntry?.savedStateHandle?.set(ITEMS_KEY, Json.encodeToString(items))
    navigate(AppRoutes.SEE_ALL)
}

fun NavController.navigateToCategory(category: HomeCategory) {
    currentBackStackEntry?.savedStateHandle?.set(CATEGORY_KEY, Json.encodeToString(category))
    navigate(AppRoutes.CATEGORY)
}

fun NavController.navigateToThemedSection(section: ThemedSection) {
    currentBackStackEntry?.savedStateHandle?.set(THEMED_SECTION_KEY, Json.encodeToString(section))
    navigate(AppRoutes.CATEGORY)
}

fun NavController.getMediaItem(handle: androidx.lifecycle.SavedStateHandle): MediaItem? {
    return handle.get<String>(MEDIA_ITEM_KEY)?.let(AppRoutes::decodeItem)
}

fun NavController.getItems(handle: androidx.lifecycle.SavedStateHandle): List<MediaItem> {
    val raw = handle.get<String>(ITEMS_KEY) ?: return emptyList()
    return Json.decodeFromString(raw)
}

fun NavController.getTitle(handle: androidx.lifecycle.SavedStateHandle): String =
    handle.get<String>(TITLE_KEY).orEmpty()

fun NavController.getCategory(handle: androidx.lifecycle.SavedStateHandle): HomeCategory? {
    val raw = handle.get<String>(CATEGORY_KEY) ?: return null
    return Json.decodeFromString(raw)
}

fun NavController.getThemedSection(handle: androidx.lifecycle.SavedStateHandle): ThemedSection? {
    val raw = handle.get<String>(THEMED_SECTION_KEY) ?: return null
    return Json.decodeFromString(raw)
}
