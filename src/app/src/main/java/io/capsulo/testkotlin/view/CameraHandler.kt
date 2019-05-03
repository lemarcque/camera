/**
 * MIT License
 *
 * <p>Copyright (C) 2019 Henoc Sese
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.capsulo.testkotlin.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.core.app.ActivityCompat
import io.capsulo.testkotlin.view.fragments.CameraFragment
import java.lang.Exception
import java.util.*

/**
 *
 * Responsible to configure and handle the Camera device.
 *
 * @property cameraFragment a references to the fragment  displayed on screen.
 * @constructor Creates an CameraHandler.
 */

// TODO : Does it lead to memory leak to have reference to cameraFragment ?
class CameraHandler(var cameraFragment: CameraFragment?) {

    // Variable
    private val TAG: String? = CameraHandler::class.simpleName

    private var cameraId: String? = null
    private var cameraManager: CameraManager = cameraFragment?.activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var captureRequest: CaptureRequest? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private lateinit var previewSize: Size
    private var cameraDevice: CameraDevice? = null                  // representation of a single camera connected to an Android device
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private lateinit var stateCallback: CameraDevice.StateCallback


    /**
     *  Initialization of a camera callback device
     *  to perform operation based on camera state
     */
    init {
        // Set CameraDevice callback
        setCameraDeviceCallback()

        // Start the running the background thread
        openBackgroundThread()
    }


    /**
     * Set the callback for camera device
     */
    private fun setCameraDeviceCallback() {

        // Set the CameraManager
        stateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: CameraDevice) {
                this@CameraHandler.cameraDevice = cameraDevice
                cameraFragment?.onCreatePreviewSession() // TODO : Should use the Obserevr pattern ?
            }

            override fun onDisconnected(cameraDevice: CameraDevice) {
                cameraDevice.close()
                this@CameraHandler.cameraDevice = null
            }

            override fun onError(cameraDevice: CameraDevice, error: Int) {
                this@CameraHandler.cameraDevice = null
            }
        }
    }


    /**
     * Start operations.
     */
    fun open() {
        setUpCamera()
        openCamera()
    }

    /**
     * Attempt to open "open" the device camera safely.
     */
    private fun setUpCamera() {
        try {
            for (cameraId: String in cameraManager.cameraIdList) {
                val cameraCharacteristics: CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing: Int? = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)

                if (facing != null) {
                    if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                        val streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture::class.java)[0]
                        this.cameraId = cameraId
                    }
                }
            }
        } catch (e: CameraAccessException) {
            Log.i(TAG, "Error : Camera not available.")
            e.printStackTrace()
        }
    }

    /**
     * Open the camera to start preview session.
     */
    private fun openCamera() {
        val context = cameraFragment?.context
        if(context != null) {
            try {
                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(cameraId, stateCallback, backgroundHandler)
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Start a background thread.
     */
    private fun openBackgroundThread() {
        backgroundThread = HandlerThread("camera_background_thread") // TODO : Use static const
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    /**
     * Create a preview session to after capture images.
     */
    fun createPreviewSession(surfaceTexture: SurfaceTexture) {
        try {

            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight())

            // Surface represents the producer side of a buffer queue
            // Buffer will shipped to the consumer
            val previewSurface = Surface(surfaceTexture)
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(previewSurface)

            // Create a new CameraCaptureSession - use for capturing images from the camera
            // or reprocessing images captured from the camera in the same session previously
            cameraDevice?.createCaptureSession(
                Collections.singletonList(previewSurface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice ==
                            null) {
                            return
                        }

                        try {
                            captureRequest = captureRequestBuilder?.build()
                            this@CameraHandler.cameraCaptureSession = cameraCaptureSession
                            this@CameraHandler.cameraCaptureSession?.setRepeatingRequest(captureRequest, null, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }

                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {

                    }
                }, backgroundHandler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    /**
     * Close all running operations.
     */
    fun close() {
        closeCamera()
        closeBackgroundThread()

        // Remove the references to avoid memory leak
        cameraFragment = null
    }

    /**
     * Close the device's camera.
     */
    private fun closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession?.close()
            // TODO cameraCaptureSession = null
        }

        if (cameraDevice != null) {
            cameraDevice?.close()
            // TODO cameraDevice = null
        }
    }

    /**
     * Close the background thread
     */
    private fun closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread?.quitSafely()
            backgroundThread = null
            // TODO backgroundHandler = null
        }
    }

}