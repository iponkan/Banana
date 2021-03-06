package com.ponkan.banana.camera;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ponkan.banana.FullScreenActivity;
import com.ponkan.banana.R;

public class CameraActivity extends FullScreenActivity implements OnFragmentInteractionListener {


    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            initFullScreen();
        }
    }

    @Override
    public void go2ImagePreview(String imagePath) {
        ImagePreviewFragment imagePreviewFragment = ImagePreviewFragment.newInstance(imagePath, "");
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, imagePreviewFragment,
                "imagePreviewFragment").addToBackStack(null).commitAllowingStateLoss();

    }

    @Override
    public void onBackPressed() {

        // 后退处理，配合addToBackStack使用
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
