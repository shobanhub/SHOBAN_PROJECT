package com.secureqrstreamer.app.gallery

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/** Procedural low-poly room and simple animated avatar; all geometry is generated locally. */
class GallerySceneRenderer : GLSurfaceView.Renderer {
    @Volatile private var orbitDegrees = 0f
    @Volatile private var avatarX = -1.35f
    @Volatile private var targetX = -1.35f
    @Volatile private var walking = false
    private var program = 0
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpHandle = 0
    private lateinit var cube: FloatBuffer
    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val model = FloatArray(16)
    private val vp = FloatArray(16)
    private val mvp = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.035f, 0.055f, 0.07f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val vertex = "attribute vec4 aPosition; uniform mat4 uMvp; void main(){ gl_Position=uMvp*aPosition; }"
        val fragment = "precision mediump float; uniform vec4 uColor; void main(){ gl_FragColor=uColor; }"
        program = link(vertex, fragment)
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        mvpHandle = GLES20.glGetUniformLocation(program, "uMvp")
        cube = ByteBuffer.allocateDirect(CUBE.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(CUBE); position(0) }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.perspectiveM(projection, 0, 52f, width.toFloat() / height.coerceAtLeast(1), 0.1f, 50f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val angle = Math.toRadians(orbitDegrees.toDouble())
        val camX = sin(angle).toFloat() * 5.3f
        val camZ = cos(angle).toFloat() * 5.3f
        Matrix.setLookAtM(view, 0, camX, 2.45f, camZ, 0f, 1.05f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vp, 0, projection, 0, view, 0)
        drawBox(0f, -0.12f, 0f, 5.8f, 0.2f, 5.8f, floatArrayOf(0.20f, 0.27f, 0.25f, 1f))
        drawBox(0f, 2.15f, -2.85f, 5.8f, 4.3f, 0.16f, floatArrayOf(0.10f, 0.16f, 0.16f, 1f))
        drawBox(-2.85f, 2.15f, 0f, 0.16f, 4.3f, 5.8f, floatArrayOf(0.08f, 0.13f, 0.14f, 1f))
        drawBox(2.85f, 2.15f, 0f, 0.16f, 4.3f, 5.8f, floatArrayOf(0.08f, 0.13f, 0.14f, 1f))
        // Gallery frames, with warm inset canvases.
        for (i in 0..3) {
            val x = -2.0f + i * 1.3f
            drawBox(x, 1.55f, -2.70f, 0.82f, 1.02f, 0.06f, floatArrayOf(0.76f, 0.53f, 0.29f, 1f))
            drawBox(x, 1.55f, -2.655f, 0.66f, 0.84f, 0.04f, palette(i))
        }
        // Avatar body/limbs. Walking toggles a gentle step animation.
        if (walking) avatarX += (targetX - avatarX) * 0.035f
        val step = if (walking && kotlin.math.abs(targetX - avatarX) > 0.04f) sin(System.nanoTime() / 90_000_000.0).toFloat() * 0.13f else 0f
        drawBox(avatarX, 0.90f, 0.45f, 0.44f, 0.70f, 0.30f, floatArrayOf(0.20f, 0.66f, 0.58f, 1f))
        drawBox(avatarX, 1.43f, 0.45f, 0.36f, 0.36f, 0.34f, floatArrayOf(0.94f, 0.72f, 0.51f, 1f))
        drawBox(avatarX - 0.31f, 0.94f + step, 0.45f, 0.16f, 0.58f, 0.18f, floatArrayOf(0.94f, 0.72f, 0.51f, 1f))
        drawBox(avatarX + 0.31f, 0.94f - step, 0.45f, 0.16f, 0.58f, 0.18f, floatArrayOf(0.94f, 0.72f, 0.51f, 1f))
        drawBox(avatarX - 0.12f, 0.39f - step, 0.45f, 0.17f, 0.48f, 0.19f, floatArrayOf(0.14f, 0.38f, 0.34f, 1f))
        drawBox(avatarX + 0.12f, 0.39f + step, 0.45f, 0.17f, 0.48f, 0.19f, floatArrayOf(0.14f, 0.38f, 0.34f, 1f))
    }

    fun orbit(delta: Float) { orbitDegrees = (orbitDegrees + delta) % 360f }
    fun moveTo(index: Int) { targetX = -1.7f + (index.coerceIn(0, 3) * 1.15f); walking = true }
    fun stopAt(index: Int) { targetX = -1.7f + (index.coerceIn(0, 3) * 1.15f); walking = false; avatarX = targetX }
    fun setWalking(value: Boolean) { walking = value }

    private fun drawBox(x: Float, y: Float, z: Float, sx: Float, sy: Float, sz: Float, color: FloatArray) {
        Matrix.setIdentityM(model, 0); Matrix.translateM(model, 0, x, y, z); Matrix.scaleM(model, 0, sx, sy, sz)
        Matrix.multiplyMM(mvp, 0, vp, 0, model, 0)
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvp, 0)
        GLES20.glUniform4fv(colorHandle, 1, color, 0)
        cube.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, cube)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    private fun palette(i: Int) = when (i) {
        0 -> floatArrayOf(0.78f, 0.36f, 0.25f, 1f)
        1 -> floatArrayOf(0.26f, 0.54f, 0.72f, 1f)
        2 -> floatArrayOf(0.86f, 0.65f, 0.31f, 1f)
        else -> floatArrayOf(0.48f, 0.62f, 0.42f, 1f)
    }

    private fun link(v: String, f: String): Int {
        fun shader(type: Int, source: String): Int = GLES20.glCreateShader(type).also {
            GLES20.glShaderSource(it, source); GLES20.glCompileShader(it)
            val status = IntArray(1); GLES20.glGetShaderiv(it, GLES20.GL_COMPILE_STATUS, status, 0)
            check(status[0] == GLES20.GL_TRUE) { GLES20.glGetShaderInfoLog(it) }
        }
        val p = GLES20.glCreateProgram(); GLES20.glAttachShader(p, shader(GLES20.GL_VERTEX_SHADER, v)); GLES20.glAttachShader(p, shader(GLES20.GL_FRAGMENT_SHADER, f)); GLES20.glLinkProgram(p)
        val status = IntArray(1); GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, status, 0); check(status[0] == GLES20.GL_TRUE) { GLES20.glGetProgramInfoLog(p) }; return p
    }

    private companion object {
        // 12 triangles, vertices for a unit cube centered at origin.
        val CUBE = floatArrayOf(
            -0.5f,-0.5f,0.5f, 0.5f,-0.5f,0.5f, 0.5f,0.5f,0.5f, -0.5f,-0.5f,0.5f, 0.5f,0.5f,0.5f, -0.5f,0.5f,0.5f,
            0.5f,-0.5f,-0.5f, -0.5f,-0.5f,-0.5f, -0.5f,0.5f,-0.5f, 0.5f,-0.5f,-0.5f, -0.5f,0.5f,-0.5f, 0.5f,0.5f,-0.5f,
            -0.5f,-0.5f,-0.5f, -0.5f,-0.5f,0.5f, -0.5f,0.5f,0.5f, -0.5f,-0.5f,-0.5f, -0.5f,0.5f,0.5f, -0.5f,0.5f,-0.5f,
            0.5f,-0.5f,0.5f, 0.5f,-0.5f,-0.5f, 0.5f,0.5f,-0.5f, 0.5f,-0.5f,0.5f, 0.5f,0.5f,-0.5f, 0.5f,0.5f,0.5f,
            -0.5f,0.5f,0.5f, 0.5f,0.5f,0.5f, 0.5f,0.5f,-0.5f, -0.5f,0.5f,0.5f, 0.5f,0.5f,-0.5f, -0.5f,0.5f,-0.5f,
            -0.5f,-0.5f,-0.5f, 0.5f,-0.5f,-0.5f, 0.5f,-0.5f,0.5f, -0.5f,-0.5f,-0.5f, 0.5f,-0.5f,0.5f, -0.5f,-0.5f,0.5f
        )
    }
}
