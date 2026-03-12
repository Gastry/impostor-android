package com.impostorparty.domain.usecase

import java.text.Normalizer
import java.util.Locale

fun normalizeWordForComparison(value: String): String {
    val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
    val primary = normalized
        .replace("\\p{Mn}+".toRegex(), "")
        .lowercase(Locale.ROOT)
        .replace("[^\\p{L}\\p{Nd}]+".toRegex(), "")

    if (primary.isNotBlank()) {
        return primary
    }

    return value
        .lowercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() }
}