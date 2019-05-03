package io.capsulo.testkotlin.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.capsulo.testkotlin.R
import io.capsulo.testkotlin.view.fragments.CameraFragment


/**
 * Responsible to display all camera features.
 */
class CameraActivity : AppCompatActivity() {


    // Variable
    private val TAG: String? = CameraActivity::class.simpleName

    /**
     * Overriding creation's method of the Activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_camera)
        supportFragmentManager
            .beginTransaction()
            .add(R.id.activity_camera, CameraFragment.newInstance())
            .commit()


    }

}
