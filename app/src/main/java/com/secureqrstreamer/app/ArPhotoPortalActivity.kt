package com.secureqrstreamer.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.ArCoreApkException
import com.google.ar.core.exceptions.UnavailableException
import com.secureqrstreamer.app.ar.ArCameraRenderer

/** ARCore capability/session preview. A stable, world-anchored portal renderer is a follow-up stage. */
class ArPhotoPortalActivity : AppCompatActivity() {
    private var session: Session? = null
    private lateinit var surface: android.opengl.GLSurfaceView
    private lateinit var renderer: ArCameraRenderer
    private lateinit var state: TextView
    private var installRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        val root = FrameLayout(this)
        surface = android.opengl.GLSurfaceView(this).apply {
            setEGLContextClientVersion(2); preserveEGLContextOnPause = true
            renderer = ArCameraRenderer { message -> runOnUiThread { state.text = message } }
            setRenderer(renderer); renderMode = android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        root.addView(surface, FrameLayout.LayoutParams(-1, -1))
        val panel = android.widget.LinearLayout(this).apply { orientation = android.widget.LinearLayout.VERTICAL; setPadding(18, 16, 18, 14); setBackgroundColor(0xB9000000.toInt()) }
        state = TextView(this).apply { text = "Checking ARCore support…"; setTextColor(Color.WHITE); textSize = 16f }
        val info = TextView(this).apply { text = "Move the phone slowly to detect surfaces. Use the standard QR flow if AR is unavailable."; setTextColor(Color.WHITE); textSize = 13f; setPadding(0, 8, 0, 8) }
        val done = MaterialButton(this).apply { text = "Close AR preview"; setOnClickListener { finish() } }
        panel.addView(state); panel.addView(info); panel.addView(done)
        root.addView(panel, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        setContentView(root)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST)
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                when (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> { installRequested = true; return }
                    ArCoreApk.InstallStatus.INSTALLED -> Unit
                }
                if (session == null) session = Session(this)
                val config = Config(session).apply { planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL }
                session?.configure(config)
                surface.onResume()
                renderer.attach(session)
                session?.resume()
                state.text = "ARCore running · scanning for horizontal and vertical planes"
            } catch (e: UnavailableException) {
                state.text = "AR unavailable on this device: ${e.localizedMessage ?: "unsupported or not installed"}. Use the 3D gallery or QR flow."
            } catch (e: ArCoreApkException) {
                state.text = "ARCore could not start. Use QR or 3D gallery instead."
            } catch (e: SecurityException) {
                state.text = "Camera permission is required for AR."
            } catch (e: Exception) {
                state.text = "AR session error. Use QR or 3D gallery instead."
            }
        }
    }

    override fun onPause() {
        try { session?.pause() } catch (_: Exception) { }
        if (::surface.isInitialized) surface.onPause()
        super.onPause()
    }

    override fun onDestroy() { session?.close(); session = null; super.onDestroy() }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) recreate()
        else if (requestCode == CAMERA_REQUEST) { state.text = "Camera permission denied. QR and 3D modes remain available." }
    }

    private companion object { const val CAMERA_REQUEST = 77 }
}
