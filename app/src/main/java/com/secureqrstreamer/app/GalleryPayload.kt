package com.secureqrstreamer.app

object GalleryPayload {
    private val idPattern = Regex("^[A-Za-z0-9_-]{8,80}$")

    fun format(galleryId: String): String? = galleryId.takeIf(idPattern::matches)?.let { "SQR1:$it" }

    fun parse(payload: String): String? {
        if (!payload.startsWith("SQR1:")) return null
        val id = payload.removePrefix("SQR1:")
        return id.takeIf(idPattern::matches)
    }
}
