package com.ponkan.banana.player

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ponkan.banana.PlayerFragment
import com.ponkan.banana.R

class PlayerActivity : AppCompatActivity(), PlayerFragment.OnFragmentInteractionListener {

    private var playerFragment: PlayerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_palyer)

        if (playerFragment == null) {
            playerFragment = PlayerFragment.newInstance("heng", "ha")
            supportFragmentManager.beginTransaction().add(android.R.id.content, playerFragment, "playerFragment").commitAllowingStateLoss()
        }
    }

    override fun onFragmentInteraction(uri: Uri) {

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        val mDecorView = window.decorView
        mDecorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
}
