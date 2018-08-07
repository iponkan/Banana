package com.ponkan.banana.camera

import android.annotation.TargetApi
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager

import com.ponkan.banana.R

class CameraActivity : AppCompatActivity(), CameraFragment.OnFragmentInteractionListener {

    private var cameraFragment: CameraFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        enableImmersive()

        if (cameraFragment == null) {
            cameraFragment = CameraFragment.newInstance("heng", "ha")
            supportFragmentManager.beginTransaction().add(android.R.id.content, cameraFragment, "cameraFragment").commitAllowingStateLoss()
        }
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun enableImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flag = window.decorView.systemUiVisibility
            flag = flag or WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            flag = flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.decorView.systemUiVisibility = flag
            window.statusBarColor = Color.TRANSPARENT
        }
    }
}
