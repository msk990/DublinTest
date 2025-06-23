package com.example.dublintest.stickers.model

enum class StickerType {
    COLLECTIBLE,
    INTERACTIVE
}

val stickerTypeMap = mapOf(
    "laptop" to StickerType.COLLECTIBLE,
    "bicycle" to StickerType.INTERACTIVE
    // Add more labels and types as needed
)
