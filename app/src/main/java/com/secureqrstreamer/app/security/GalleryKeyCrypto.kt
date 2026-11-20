package com.secureqrstreamer.app.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Standalone crypto primitives for a future compatible gallery envelope. No cloud format is defined yet. */
object GalleryKeyCrypto {
    private const val ITERATIONS = 210_000
    private const val KEY_BITS = 256
    private const val GCM_TAG_BITS = 128
    private val random = SecureRandom()

    data class WrappedKey(val salt: ByteArray, val nonce: ByteArray, val ciphertext: ByteArray)

    fun wrapKey(galleryKey: ByteArray, recoverySecret: CharArray): WrappedKey {
        require(galleryKey.size == 32) { "Gallery key must be 256 bits" }
        val salt = ByteArray(16).also(random::nextBytes)
        val nonce = ByteArray(12).also(random::nextBytes)
        val wrappingKey = derive(recoverySecret, salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(wrappingKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, nonce))
            WrappedKey(salt, nonce, cipher.doFinal(galleryKey))
        } finally {
            wrappingKey.fill(0)
            recoverySecret.fill('\u0000')
        }
    }

    fun unwrapKey(wrapped: WrappedKey, recoverySecret: CharArray): ByteArray {
        val wrappingKey = derive(recoverySecret, wrapped.salt)
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(wrappingKey, "AES"), GCMParameterSpec(GCM_TAG_BITS, wrapped.nonce))
            cipher.doFinal(wrapped.ciphertext)
        } finally {
            wrappingKey.fill(0)
            recoverySecret.fill('\u0000')
        }
    }

    fun selfTest(): Boolean {
        val key = ByteArray(32).also(random::nextBytes)
        val secret = "test-only-recovery-secret".toCharArray()
        val wrapped = wrapKey(key, secret)
        val unwrapped = unwrapKey(wrapped, "test-only-recovery-secret".toCharArray())
        val passed = java.security.MessageDigest.isEqual(key, unwrapped)
        key.fill(0)
        unwrapped.fill(0)
        return passed
    }

    private fun derive(secret: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(secret, salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
