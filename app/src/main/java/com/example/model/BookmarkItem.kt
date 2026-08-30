package com.example.model

/**
 * Model representing a bookmarked website in Download Master browser.
 */
data class BookmarkItem(
    val id: String,
    val title: String,
    val url: String,
    val iconUrl: String? = null
)
