package com.secureqrstreamer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class GalleryFlowTest {
    @Test fun validPayloadFormatsAndParses() {
        assertEquals("SQR1:gallery_123", GalleryPayload.format("gallery_123"))
        assertEquals("gallery_123", GalleryPayload.parse("SQR1:gallery_123"))
    }
    @Test fun invalidPayloadsAreRejected() {
        assertNull(GalleryPayload.parse("QR2:gallery_123"))
        assertNull(GalleryPayload.parse("SQR1:bad id"))
        assertNull(GalleryPayload.parse("SQR1:"))
    }
}
