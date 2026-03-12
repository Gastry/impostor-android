package com.impostorparty.domain.repository

import com.impostorparty.domain.model.LocalizedWordPool

interface WordRepository {
    suspend fun getWords(languageTag: String?): LocalizedWordPool
}
