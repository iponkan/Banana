package com.ponkan.banana

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ponkan.banana.audio.AudioActivity
import com.ponkan.banana.camera.CameraActivity
import com.ponkan.banana.player.PlayerActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goAudio(view: View) {
        val intent =  Intent()
        intent.setClass(this, AudioActivity::class.java)
        startActivity(intent)

    }

    fun goCamera(view: View) {
        val intent =  Intent()
        intent.setClass(this, CameraActivity::class.java)
        startActivity(intent)

    }

    fun goPlayer(view: View) {
        val intent =  Intent()
        intent.setClass(this, PlayerActivity::class.java)
        startActivity(intent)

    }
}
