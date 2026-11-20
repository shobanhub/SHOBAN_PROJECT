package com.secureqrstreamer.app.core.security

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Stores only a salted verifier for this device's local access gate. */
class LocalPinStore(context: Context) {
    private val prefs = context.getSharedPreferences("local_pin_v1", Context.MODE_PRIVATE)
    private val random = SecureRandom()

    fun hasPin(): Boolean = prefs.contains(KEY_HASH) && prefs.contains(KEY_SALT)

    fun setPin(pin: String) {
        require(pin.length in 6..12 && pin.all(Char::isDigit))
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = derive(pin.toCharArray(), salt)
        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .apply()
        hash.fill(0)
    }

    fun verify(pin: String): Boolean {
        val salt = prefs.getString(KEY_SALT, null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
        val expected = prefs.getString(KEY_HASH, null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
        val actual = derive(pin.toCharArray(), salt)
        val equal = java.security.MessageDigest.isEqual(expected, actual)
        expected.fill(0)
        actual.fill(0)
        return equal
    }

    private fun derive(pin: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin, salt, ITERATIONS, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            pin.fill('\u0000')
        }
    }

    private companion object {
        const val KEY_SALT = "pin_salt"
        const val KEY_HASH = "pin_hash"
        const val SALT_BYTES = 16
        const val ITERATIONS = 120_000
        const val KEY_BITS = 256
    }
}
