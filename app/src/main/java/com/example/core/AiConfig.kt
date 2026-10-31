package com.example.core

object AiConfig {
    const val PRIMARY_MODEL = "gemini-3.1-flash-lite"
    val FALLBACK_MODELS = listOf("gemini-3.8-flash", "gemini-3.5-flash")
    const val TIMEOUT_SECONDS = 60L
    const val FREE_DAILY_MESSAGE_LIMIT = 5
}
