package com.secureqrstreamer.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.secureqrstreamer.app.databinding.ActivityUiFlowBinding

/** Demonstrates the authorized recovery UI sequence without displaying private media. */
class UiFlowActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUiFlowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        binding = ActivityUiFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
        binding.galleryPayloadInput.addTextChangedListener { binding.statusText.text = "Validate the QR payload before asking for PIN or recovery access." }

        binding.validateButton.setOnClickListener {
            val id = GalleryPayload.parse(binding.galleryPayloadInput.text?.toString().orEmpty())
            if (id == null) {
                binding.statusText.text = "Invalid gallery QR. Expected SQR1:<public-gallery-id>."
            } else {
                binding.statusText.text = "QR valid for $id. Next: verify the device-local PIN."
                promptPin(id)
            }
        }

        binding.pinButton.setOnClickListener { promptPin(null) }
        binding.recoveryButton.setOnClickListener { promptRecoverySecret() }
        binding.viewer2dButton.setOnClickListener { startActivity(Intent(this, GalleryActivity::class.java)) }
        binding.virtualButton.setOnClickListener { startActivity(Intent(this, VirtualGalleryActivity::class.java)) }
        binding.arButton.setOnClickListener { startActivity(Intent(this, ArPhotoPortalActivity::class.java)) }
    }

    private fun promptPin(galleryId: String?) {
        val pin = TextInputEditText(this).apply { hint = "Device-local PIN"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        MaterialAlertDialogBuilder(this)
            .setTitle("Verify PIN")
            .setMessage("PIN verification is a local access gate. It is not a recovery secret or gallery key.")
            .setView(pin)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Verify") { _, _ ->
                val store = com.secureqrstreamer.app.core.security.LocalPinStore(this)
                if (!store.hasPin()) binding.statusText.text = "No PIN is configured on this device. Set it from the home screen."
                else if (!store.verify(pin.text?.toString().orEmpty())) binding.statusText.text = "Incorrect PIN. Gallery access remains closed."
                else {
                    binding.statusText.text = "PIN verified${if (galleryId != null) " for $galleryId" else ""}. Next: enter the separate recovery secret."
                    promptRecoverySecret()
                }
            }.show()
    }

    private fun promptRecoverySecret() {
        val secret = TextInputEditText(this).apply { hint = "Recovery secret" }
        MaterialAlertDialogBuilder(this)
            .setTitle("Recover gallery key")
            .setMessage("The recovery secret is separate from the PIN and must never be placed in a QR payload.")
            .setView(secret)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ ->
                if (secret.text.isNullOrBlank()) binding.statusText.text = "Recovery secret is required."
                else binding.statusText.text = "Recovery authorization UI complete. Cloud manifest lookup, key unwrap, download, and decryption remain to be integrated."
            }.show()
    }
}
