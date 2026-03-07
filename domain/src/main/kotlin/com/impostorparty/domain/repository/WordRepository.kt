package com.impostorparty.domain.repository

import com.impostorparty.domain.model.WordEntry

interface WordRepository {
    suspend fun getWords(): List<WordEntry>
}