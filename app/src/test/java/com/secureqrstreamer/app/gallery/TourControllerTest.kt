package com.secureqrstreamer.app.gallery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TourControllerTest {
    @Test fun advancesAndWraps() {
        val tour = TourController(2)
        assertEquals(1, tour.next())
        assertEquals(0, tour.next())
    }
    @Test fun playPauseAndEmptyTour() {
        val tour = TourController(1)
        tour.play(); assertTrue(tour.isPlaying)
        tour.pause(); assertFalse(tour.isPlaying)
        assertEquals(null, TourController(0).next())
    }
}
