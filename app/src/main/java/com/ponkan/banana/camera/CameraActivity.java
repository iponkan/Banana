package com.ponkan.banana.camera;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ponkan.banana.FullScreenActivity;
import com.ponkan.banana.R;

public class CameraActivity extends FullScreenActivity implements
        CameraFragment.OnFragmentInteractionListener {


    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFullScreen();
        setContentView(R.layout.activity_camera);

        if (cameraFragment == null) {
            cameraFragment = CameraFragment.newInstance("heng", "ha");
            getSupportFragmentManager().beginTransaction().add(android.R.id.content, cameraFragment,
                    "cameraFragment").commitAllowingStateLoss();
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


}
