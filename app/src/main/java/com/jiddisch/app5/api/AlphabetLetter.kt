package com.jiddisch.app5.api

data class AlphabetLetter(
    val id: Int,
    val yiddish_letter: String,
    val yiddish_letter_name: String,
    val latin_letter_name: String,
    val sort: Int,
    val transcription: List<String>,
    val voice: String?,
)
