package com.jiddisch.app5.api

data class Word(
    val id: Int,
    val image: String,
    val english_word: String,
    val yiddish_word: String,
    val indefinite_article: String?,
    val plural_form: String?,
    val yiddish_transcription: String,
    val example_sentence: String?,
    val category: Int,
    val voice: String?,
)

