package com.secureqrstreamer.app.gallery

/** Small deterministic tour state model used by the 3D gallery controls. */
class TourController(private val itemCount: Int) {
    var currentIndex: Int = 0
        private set
    var isPlaying: Boolean = false
        private set

    init { require(itemCount >= 0) }
    fun play() { if (itemCount > 0) isPlaying = true }
    fun pause() { isPlaying = false }
    fun next(): Int? {
        if (itemCount == 0) return null
        currentIndex = (currentIndex + 1) % itemCount
        return currentIndex
    }
    fun previous(): Int? {
        if (itemCount == 0) return null
        currentIndex = (currentIndex - 1 + itemCount) % itemCount
        return currentIndex
    }
}
