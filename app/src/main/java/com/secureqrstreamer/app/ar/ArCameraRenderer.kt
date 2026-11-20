package com.secureqrstreamer.app.ar

import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.google.ar.core.Coordinates2d
import com.google.ar.core.Session
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** Draws the live ARCore camera background and reports tracking state; no image is saved. */
class ArCameraRenderer(private val onStatus: (String) -> Unit) : GLSurfaceView.Renderer {
    @Volatile private var session: Session? = null
    private var textureId = 0
    private var program = 0
    private var position = 0
    private var uv = 0
    private var sampler = 0
    private lateinit var vertices: FloatBuffer
    private lateinit var texCoords: FloatBuffer
    private val transformed = FloatArray(8)
    private var lastStatusTime = 0L

    fun attach(value: Session?) { session = value; if (textureId != 0) value?.setCameraTextureNames(intArrayOf(textureId)) }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val ids = IntArray(1); GLES20.glGenTextures(1, ids, 0); textureId = ids[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        val vertex = "attribute vec4 aPosition; attribute vec2 aUv; varying vec2 vUv; void main(){ gl_Position=aPosition; vUv=aUv; }"
        val fragment = "#extension GL_OES_EGL_image_external : require\nprecision mediump float; varying vec2 vUv; uniform samplerExternalOES sTexture; void main(){ gl_FragColor=texture2D(sTexture,vUv); }"
        program = link(vertex, fragment); position = GLES20.glGetAttribLocation(program, "aPosition"); uv = GLES20.glGetAttribLocation(program, "aUv"); sampler = GLES20.glGetUniformLocation(program, "sTexture")
        vertices = buffer(floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, 1f,1f))
        texCoords = buffer(FloatArray(8))
        session?.setCameraTextureNames(intArrayOf(textureId))
        GLES20.glClearColor(0f, 0f, 0f, 1f)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) { GLES20.glViewport(0, 0, width, height) }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val active = session ?: return
        try {
            val frame = active.update()
            frame.transformCoordinates2d(Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES, floatArrayOf(-1f,-1f, 1f,-1f, -1f,1f, 1f,1f), Coordinates2d.TEXTURE_NORMALIZED, transformed)
            texCoords.clear(); texCoords.put(transformed); texCoords.position(0)
            GLES20.glUseProgram(program); GLES20.glActiveTexture(GLES20.GL_TEXTURE0); GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId); GLES20.glUniform1i(sampler, 0)
            vertices.position(0); GLES20.glEnableVertexAttribArray(position); GLES20.glVertexAttribPointer(position, 2, GLES20.GL_FLOAT, false, 0, vertices)
            texCoords.position(0); GLES20.glEnableVertexAttribArray(uv); GLES20.glVertexAttribPointer(uv, 2, GLES20.GL_FLOAT, false, 0, texCoords)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4); GLES20.glDisableVertexAttribArray(position); GLES20.glDisableVertexAttribArray(uv)
            val now = System.currentTimeMillis()
            if (now - lastStatusTime > 900) {
                lastStatusTime = now
                val state = frame.camera.trackingState.name
                onStatus(if (state == "TRACKING") "AR tracking · move slowly and scan for a frame or surface" else "AR state: ${state.lowercase().replaceFirstChar { it.uppercase() }} · move the phone slowly")
            }
        } catch (_: Exception) {
            onStatus("AR camera is starting or tracking was interrupted. Point the camera at a textured, well-lit surface.")
        }
    }

    private fun buffer(values: FloatArray) = ByteBuffer.allocateDirect(values.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply { put(values); position(0) }
    private fun link(v: String, f: String): Int {
        fun compile(type: Int, src: String): Int = GLES20.glCreateShader(type).also { GLES20.glShaderSource(it, src); GLES20.glCompileShader(it); val result = IntArray(1); GLES20.glGetShaderiv(it, GLES20.GL_COMPILE_STATUS, result, 0); check(result[0] == GLES20.GL_TRUE) { GLES20.glGetShaderInfoLog(it) } }
        val p = GLES20.glCreateProgram(); GLES20.glAttachShader(p, compile(GLES20.GL_VERTEX_SHADER, v)); GLES20.glAttachShader(p, compile(GLES20.GL_FRAGMENT_SHADER, f)); GLES20.glLinkProgram(p); val result = IntArray(1); GLES20.glGetProgramiv(p, GLES20.GL_LINK_STATUS, result, 0); check(result[0] == GLES20.GL_TRUE) { GLES20.glGetProgramInfoLog(p) }; return p
    }
}
