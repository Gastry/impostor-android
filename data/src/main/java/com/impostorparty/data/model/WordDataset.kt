package com.impostorparty.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WordDataset(
    val categories: List<WordCategoryPayload>,
)

@Serializable
data class WordCategoryPayload(
    val code: String,
    val words: List<String>,
)