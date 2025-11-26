package com.example.freshcookapp.util

import java.text.Normalizer

object TextUtils {
    // Normalize for deduplication: trim, lowercase, collapse whitespace and remove diacritics
    fun normalizeKey(input: String): String {
        val trimmed = input.trim().lowercase()
        val collapsed = trimmed.replace("\\s+".toRegex(), " ")
        val normalized = Normalizer.normalize(collapsed, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
}
