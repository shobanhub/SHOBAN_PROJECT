package com.secureqrstreamer.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.secureqrstreamer.app.databinding.ActivityGalleryBinding

class GalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGalleryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.galleryTitle.text = "Gallery preview"
    }
}
