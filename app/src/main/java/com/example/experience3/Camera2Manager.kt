package com.example.experience3

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CameraCaptureSession.StateCallback
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.Log
import android.util.Range
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import kotlin.math.abs

class Camera2Manager(private val context: Context) {

    companion object {
        private const val TAG = "Camera2Manager"
    }

    /**
     * 相机管理器
     */
    private var manager: CameraManager = context.getSystemService(CameraManager::class.java)

    /**
     * 相机实体类
     */
    private lateinit var cameraDevice: CameraDevice

    /**
     * Surface列表
     */
    private lateinit var surfaceList: List<Surface>

    /**
     * 相机会话
     */
    private lateinit var cameraCaptureSession: CameraCaptureSession

    /**ø
     * 相机状态回调
     */
    private val cameraDeviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "相机打开")
            cameraDevice = camera
            cameraDevice.createCaptureSession(surfaceList, stateCallback, null)
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "相机失去连接")
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(TAG, "相机错误: $error")
        }
    }

    /**
     * 相机回话回调
     */
    private val stateCallback = object : StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            Log.d(TAG, "相机会话配置成功")
            cameraCaptureSession = session
            val builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            // 自动对焦
            builder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            // 自动曝光
            builder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            // fps
            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range.create(60, 60))

            //builder.set(CaptureRequest.SCALER_CROP_REGION, Rect(0, 0, 2000, 3000))

            surfaceList.forEach {
                builder.addTarget(it)
            }
            cameraCaptureSession.setRepeatingRequest(builder.build(), object : CaptureCallback() {
                override fun onCaptureStarted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    timestamp: Long,
                    frameNumber: Long
                ) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber)
                    //Log.d(TAG, "onCaptureStarted: $frameNumber")
                }

                override fun onCaptureProgressed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    partialResult: CaptureResult
                ) {
                    super.onCaptureProgressed(session, request, partialResult)
                }

                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                }

                override fun onCaptureFailed(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    failure: CaptureFailure
                ) {
                    super.onCaptureFailed(session, request, failure)
                }

                override fun onCaptureSequenceCompleted(
                    session: CameraCaptureSession,
                    sequenceId: Int,
                    frameNumber: Long
                ) {
                    super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
                }

                override fun onCaptureSequenceAborted(
                    session: CameraCaptureSession,
                    sequenceId: Int
                ) {
                    super.onCaptureSequenceAborted(session, sequenceId)
                }

                override fun onCaptureBufferLost(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    target: Surface,
                    frameNumber: Long
                ) {
                    super.onCaptureBufferLost(session, request, target, frameNumber)
                }
            }, null)
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.d(TAG, "相机会话配置失败")
        }
    }


    fun getCameraList(): Array<String> {
        return manager.cameraIdList
    }

    fun openCamera(cameraId: String, vararg surfaces: Surface) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        surfaceList = surfaces.toList()
        manager.openCamera(cameraId, cameraDeviceStateCallback, null)
    }


    fun getBestSize(cameraId: String, width: Int, height: Int, clazz: Class<*>): Size? {
        val ratio = width * 1f / height
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportSize = map?.getOutputSizes(clazz)
        if (supportSize.isNullOrEmpty()) return null

        var bestSize = supportSize[0]
        supportSize.forEach {
            Log.d(TAG, "supportSize: $it")
            val bestSizeRatio = bestSize.width * 1f / bestSize.height
            val currSizeRatio = it.width * 1f / it.height

            val condition1 = abs(currSizeRatio - ratio) < abs(bestSizeRatio - ratio)
            val condition2 = it.width <= width
            val condition3 = it.height <= height
            if (condition1 && condition2 && condition3) bestSize = it
        }
        return bestSize
    }
}