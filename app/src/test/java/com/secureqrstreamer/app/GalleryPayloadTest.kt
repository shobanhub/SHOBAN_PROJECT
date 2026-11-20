package com.secureqrstreamer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GalleryPayloadTest {
    @Test fun parsesValidPayload() { assertEquals("abc_DEF-123", GalleryPayload.parse("SQR1:abc_DEF-123")) }
    @Test fun rejectsWrongVersion() { assertNull(GalleryPayload.parse("SQR2:abc_DEF-123")) }
    @Test fun rejectsShortOrMalformedId() {
        assertNull(GalleryPayload.parse("SQR1:short"))
        assertNull(GalleryPayload.parse("SQR1:invalid id!"))
    }
}
