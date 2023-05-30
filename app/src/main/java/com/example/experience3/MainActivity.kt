package com.example.experience3

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }

        val manager = Camera2Manager(this)

        val ttt = findViewById<FloatTextureView>(R.id.textureView)
        ttt.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    Log.d(TAG, "width: $width  height: $height")

                    manager.getBestSize("0", width, height, surface::class.java)
                    surface.setDefaultBufferSize(1200, 1200)

                    val matrix = Matrix()
                    matrix.setScale(20f,0.5f)
                    ttt.setTransform(matrix)


                    manager.openCamera("0", Surface(surface))
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    //TODO("Not yet implemented")
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    // TODO("Not yet implemented")
                    return false
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                    //TODO("Not yet implemented")
                }

            }

        findViewById<Button>(R.id.aaaa).setOnClickListener {
            ttt.setGravity(Gravity.START or Gravity.TOP, true)
        }
        findViewById<Button>(R.id.aaaa).setOnLongClickListener {
            ttt.setGravity(Gravity.START or Gravity.TOP)
            false
        }

        findViewById<Button>(R.id.bbbb).setOnClickListener {
            ttt.setGravity(Gravity.END or Gravity.TOP, true)
        }
        findViewById<Button>(R.id.bbbb).setOnLongClickListener {
            ttt.setGravity(Gravity.END or Gravity.TOP)
            false
        }

        findViewById<Button>(R.id.cccc).setOnClickListener {
            ttt.setGravity(Gravity.END or Gravity.BOTTOM, true)
        }
        findViewById<Button>(R.id.cccc).setOnLongClickListener {
            ttt.setGravity(Gravity.END or Gravity.BOTTOM)
            false
        }

        findViewById<Button>(R.id.dddd).setOnClickListener {
            ttt.setGravity(Gravity.START or Gravity.BOTTOM, true)
        }
        findViewById<Button>(R.id.dddd).setOnLongClickListener {
            ttt.setGravity(Gravity.START or Gravity.BOTTOM)
            false
        }
    }
}