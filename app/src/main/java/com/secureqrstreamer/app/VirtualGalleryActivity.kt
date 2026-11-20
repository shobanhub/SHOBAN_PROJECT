package com.secureqrstreamer.app

import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.secureqrstreamer.app.gallery.GallerySceneRenderer
import com.secureqrstreamer.app.gallery.TourController

class VirtualGalleryActivity : AppCompatActivity() {
    private lateinit var surface: GLSurfaceView
    private lateinit var renderer: GallerySceneRenderer
    private val tour = TourController(4)
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(9, 15, 18)) }
        status = TextView(this).apply { text = "3D Memory Gallery · drag to look around"; setTextColor(Color.WHITE); textSize = 16f; gravity = Gravity.CENTER; setPadding(12, 14, 12, 14) }
        surface = GLSurfaceView(this).apply { setEGLContextClientVersion(2); renderer = GallerySceneRenderer(); setRenderer(renderer); renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY }
        val controls = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.HORIZONTAL }
        controls.addView(button("‹") { val i = tour.previous(); i?.let { renderer.moveTo(it); status.text = "Tour · memory ${it + 1} of 4" } })
        controls.addView(button("Play tour") { tour.play(); val i = tour.currentIndex; renderer.moveTo(i); status.text = "Tour playing · memory ${i + 1} of 4" })
        controls.addView(button("Pause") { tour.pause(); renderer.setWalking(false); status.text = "Tour paused · explore manually by dragging" })
        controls.addView(button("Next ›") { val i = tour.next(); i?.let { renderer.moveTo(it); status.text = "Tour · memory ${it + 1} of 4" } })
        root.addView(status, LinearLayout.LayoutParams(-1, -2)); root.addView(surface, LinearLayout.LayoutParams(-1, 0, 1f)); root.addView(controls, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
        var lastX = 0f
        surface.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { lastX = event.x; true }
                MotionEvent.ACTION_MOVE -> { val dx = event.x - lastX; lastX = event.x; renderer.orbit(dx * 0.35f); true }
                else -> true
            }
        }
    }

    override fun onResume() { super.onResume(); if (::surface.isInitialized) surface.onResume() }
    override fun onPause() { if (::surface.isInitialized) surface.onPause(); super.onPause() }

    private fun button(label: String, action: () -> Unit) = MaterialButton(this).apply {
        text = label; textSize = 12f; setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(0, 56, 1f).apply { setMargins(3, 5, 3, 8) }
    }
}
