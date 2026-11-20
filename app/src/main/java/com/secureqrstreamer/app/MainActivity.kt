package com.secureqrstreamer.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.secureqrstreamer.app.core.security.LocalPinStore
import com.secureqrstreamer.app.databinding.ActivityMainBinding
import com.secureqrstreamer.app.security.GalleryKeyCrypto

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val pinStore by lazy { LocalPinStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createGalleryButton.setOnClickListener {
            Toast.makeText(this, "Gallery hosting is not connected in this starter build.", Toast.LENGTH_LONG).show()
        }
        binding.openGalleryButton.setOnClickListener { promptGalleryId() }
        binding.pinButton.setOnClickListener { promptSetPin() }
        binding.cryptoTestButton.setOnClickListener {
            val passed = runCatching { GalleryKeyCrypto.selfTest() }.getOrDefault(false)
            Toast.makeText(this, if (passed) "Local crypto self-test passed" else "Local crypto self-test failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun promptGalleryId() {
        val input = TextInputEditText(this).apply { hint = "SQR1:public-gallery-id" }
        MaterialAlertDialogBuilder(this)
            .setTitle("Open a gallery")
            .setMessage("Enter a gallery QR payload. This starter app does not yet resolve cloud galleries.")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ ->
                val payload = input.text?.toString().orEmpty()
                val id = GalleryPayload.parse(payload)
                if (id == null) Toast.makeText(this, "Invalid gallery code", Toast.LENGTH_SHORT).show()
                else Toast.makeText(this, "Gallery ID validated: $id. Cloud recovery is not connected.", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun promptSetPin() {
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 8, 48, 0)
        }
        val first = TextInputEditText(this).apply { hint = "New PIN (6–12 digits)"; inputType = 2 }
        val second = TextInputEditText(this).apply { hint = "Confirm PIN"; inputType = 2 }
        container.addView(first)
        container.addView(second)
        MaterialAlertDialogBuilder(this)
            .setTitle("Set device-local PIN")
            .setMessage("This PIN is stored on this device and is separate from a gallery recovery secret.")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val pin = first.text?.toString().orEmpty()
                        if (pin.length !in 6..12 || pin.any { !it.isDigit() }) {
                            first.error = "Enter 6–12 digits"
                            return@setOnClickListener
                        }
                        if (pin != second.text?.toString()) {
                            second.error = "PINs do not match"
                            return@setOnClickListener
                        }
                        pinStore.setPin(pin)
                        dialog.dismiss()
                        Toast.makeText(this, "Device PIN saved", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.show()
            }
    }
}
