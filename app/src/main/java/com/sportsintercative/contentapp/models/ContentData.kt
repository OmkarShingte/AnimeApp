package com.sportsintercative.contentapp.models

data class ContentData(
    val title: String,
    val description: String,
    val imageList: List<ImageItem>,
    val videoId: String
)
