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

package io.capsulo.testkotlin.data.file

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import io.capsulo.testkotlin.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utils used to stoe images in local storage.
 */

class FileManager {


    companion object {
        val TAG: String? = File::class.simpleName

        /**
         * Attempt to create a directory on disk for storing images.
         */
        fun createImageFolder(context: Context) {

            // Verify that the directory exists
            if(!getGalleryFolder(context).exists()) {
                val isDirectoryCreated: Boolean = getGalleryFolder(context).mkdirs()
                if(!isDirectoryCreated) {
                    Log.i(TAG, "Impossible to create the directory of gallery : ${getGalleryFolder(context).absolutePath}")
                }
            }
        }

        /**
         * Create the image File object of the pictures taken.
         */
        private fun createImageFile(context: Context): File {

            // Create the image folder
            createImageFolder(context)

            val dateFormat = SimpleDateFormat("HHmmss_ddMMyyyy", Locale.getDefault())
            val imageName = dateFormat.format(Date())
            return File(getGalleryFolder(context).absolutePath + "/" + imageName + ".jpg")
        }

        /**
         * Get the File object of the gallery folder in the device.
         */
        private fun getGalleryFolder(context: Context): File {
            // Get the standard OS directory in which pictures are available to user
            // Environment object Provides access to environment variables.
            val storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

            // Create the directory in which picture will be stored
            val appGallerDirectory = File(storageDirectory, context.getString(R.string.app_name))

            return appGallerDirectory
        }

        /**
         * Save the photos in the local storage.
         * @return a boolean indicating if the image was correctly saved.
         */
        fun savePhoto(context: Context, bitmap: Bitmap): Boolean {
            var fos: FileOutputStream? = null

            try {
                // writing bitmap in the file
                fos = FileOutputStream(createImageFile(context))
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                return true
            }catch (e: IOException) {
                e.printStackTrace()
            }finally {
                try {
                    fos?.close()
                }catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            return false
        }
    }

}