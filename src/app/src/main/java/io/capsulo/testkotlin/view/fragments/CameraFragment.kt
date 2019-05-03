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
package io.capsulo.testkotlin.view.fragments

import android.Manifest
import android.app.Activity
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import io.capsulo.testkotlin.R
import io.capsulo.testkotlin.data.file.FileManager
import io.capsulo.testkotlin.view.CameraHandler


/**
 * Display images captured from camera's device.
 */
class CameraFragment : Fragment() {

    // Constant
    private val TAG: String? = CameraFragment::class.simpleName
    private val CAMERA_REQUEST_CODE: Int = 100

    // Variable
    private var cameraHandler: CameraHandler? = null

    // Views
    private var btnShoot: TextView? = null
    private lateinit var toolbar: Toolbar
    private lateinit var textureView: TextureView



    /**
     * Companion object to perform top-level class operation
     */
    companion object {

        /**
         * companion function that return an instance of CameraFragment
         */
        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }

    /**
     * Called at the creation of the Voew
     * @param inflater the ojject that will inflate the layout ressource
     * @param container the container in which view will be displayed
     * @param savedInstanceState a saved instate state
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_camera, container, false)
        return v
    }

    /**
     * Called immediately after onCreateView,
     * but before any saved state has been restored in to the view.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { bindView(); configurePermission()}

    /**
     *  Making request permission to the user.
     */
    private fun configurePermission() {
        val activity: Activity = activity as Activity
        ActivityCompat.requestPermissions(activity, arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_REQUEST_CODE)
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Open the camera at the resume lifecycle state.
     */
    override fun onResume() {
        super.onResume()

        // Create an instance of CameraHandler
        cameraHandler = CameraHandler(this)

        // Open the camera as soon TextureView is available
        if (textureView.isAvailable) {
            cameraHandler?.open()
        } else {

            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    /**
     * Bind View with their data.
     */
    private fun bindView() {

        val cameraActivity = activity
        if(cameraActivity != null) {
            // Setup app bar
            toolbar = cameraActivity.findViewById(R.id.toolbar_main)
            toolbar.visibility = View.GONE
            cameraActivity.setActionBar(toolbar)

            // Setup the shoot button
            btnShoot = cameraActivity.findViewById(R.id.btn_shoot_main)
            btnShoot?.setOnClickListener {
                // TODO : Should use a Reopsitory instead of calling this class
                if(FileManager.savePhoto(cameraActivity.baseContext, textureView.bitmap)) {
                    // SHow a Toast message
                    Toast.makeText(cameraActivity, "Your image was saved.", Toast.LENGTH_SHORT).show()
                }
            }

            // Setup the TextureView
            textureView = cameraActivity.findViewById(R.id.textureview_main)
        }
    }

    /**
     * Called when the Fragment is no longer started.
     */
    override fun onStop() {
        super.onStop()
        cameraHandler?.close()
    }

    /**
     * Callback called when we are ready to capture image
     */
    fun onCreatePreviewSession() {
        cameraHandler?.createPreviewSession(textureView.surfaceTexture)
    }

    /**
     * Surface Texture listener
     */
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) { cameraHandler?.open() }
        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = false
        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
    }

}