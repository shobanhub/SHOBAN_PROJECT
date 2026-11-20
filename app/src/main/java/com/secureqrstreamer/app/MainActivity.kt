package com.secureqrstreamer.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
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

        binding.createGalleryButton.setOnClickListener { startActivity(Intent(this, UiFlowActivity::class.java)) }
        binding.openGalleryButton.setOnClickListener { startActivity(Intent(this, UiFlowActivity::class.java)) }
        binding.pinButton.setOnClickListener { promptSetPin() }
        binding.virtualGalleryButton.setOnClickListener { startActivity(Intent(this, VirtualGalleryActivity::class.java)) }
        binding.arPortalButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this, ArPhotoPortalActivity::class.java))
            } else requestPermissions(arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA)
        }
        binding.cryptoTestButton.setOnClickListener {
            val passed = runCatching { GalleryKeyCrypto.selfTest() }.getOrDefault(false)
            Toast.makeText(this, if (passed) "Local crypto self-test passed" else "Local crypto self-test failed", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA && grantResults.firstOrNull() == android.content.pm.PackageManager.PERMISSION_GRANTED) startActivity(Intent(this, ArPhotoPortalActivity::class.java))
        else if (requestCode == REQUEST_CAMERA) Toast.makeText(this, "Camera permission denied. QR and 3D modes remain available.", Toast.LENGTH_LONG).show()
    }

    private fun promptSetPin() {
        val container = android.widget.LinearLayout(this).apply { orientation = android.widget.LinearLayout.VERTICAL; setPadding(48, 8, 48, 0) }
        val first = TextInputEditText(this).apply { hint = "New PIN (6–12 digits)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        val second = TextInputEditText(this).apply { hint = "Confirm PIN"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        container.addView(first); container.addView(second)
        val dialog = MaterialAlertDialogBuilder(this).setTitle("Set device-local PIN").setMessage("This PIN is stored on this device and is separate from a gallery recovery secret.").setView(container).setNegativeButton("Cancel", null).setPositiveButton("Save", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pin = first.text?.toString().orEmpty()
                if (pin.length !in 6..12 || pin.any { !it.isDigit() }) { first.error = "Enter 6–12 digits"; return@setOnClickListener }
                if (pin != second.text?.toString()) { second.error = "PINs do not match"; return@setOnClickListener }
                pinStore.setPin(pin); dialog.dismiss(); Toast.makeText(this, "Device PIN saved", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private companion object { const val REQUEST_CAMERA = 41 }
}
