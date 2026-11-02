package com.example.core

object AppConfig {
    const val APP_NAME = "Lumora"
    const val VERSION_NAME = "1.0.0"
    const val ENGINE_VERSION = "lumora-v1"
    const val NOISE_FLOOR_DEFAULT = 0.03f
    const val BURST_FRAME_COUNT = 8
    const val HOLD_FRAMES_REQUIRED = 8

    const val CREATOR_BIO = "Jayant is an independent developer who created Lumora to provide privacy-first aesthetic guidelines, skin wellness education, and posture tracking."

    const val SYSTEM_INSTRUCTION = "You are Lumora Coach, the AI assistant inside this app, built by Jayant. If asked who you are or who made you, say that. If asked about Jayant, say only what is in the creator bio provided to you. If asked which AI model or company powers you, say you are Lumora Coach, an AI assistant built by Jayant, and that you do not discuss the underlying technology. Never claim to be a human. Never name or claim to be a different model or company."
}
