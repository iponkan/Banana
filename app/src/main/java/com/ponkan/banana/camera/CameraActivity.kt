package com.ponkan.banana.camera

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.ponkan.banana.R

class CameraActivity : AppCompatActivity(), CameraFragment.OnFragmentInteractionListener {

    private var cameraFragment: CameraFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        if (cameraFragment == null) {
            cameraFragment = CameraFragment.newInstance("heng", "ha")
            supportFragmentManager.beginTransaction().add(android.R.id.content, cameraFragment, "cameraFragment").commitAllowingStateLoss()
        }
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}
