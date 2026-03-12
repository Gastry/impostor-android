package com.impostorparty.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WordDataset(
    val fallbackLanguage: String,
    val languages: List<LanguagePayload>,
)

@Serializable
data class LanguagePayload(
    val tag: String,
    val categories: List<WordCategoryPayload>,
)

@Serializable
data class WordCategoryPayload(
    val code: String,
    val words: List<String>,
)
