package com.impostorparty.domain.usecase

import java.text.Normalizer
import java.util.Locale

fun normalizeWordForComparison(value: String): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
    return normalized
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase(Locale.ROOT)
        .replace("[^a-z0-9]+".toRegex(), "")
}