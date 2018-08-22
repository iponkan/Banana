package com.ponkan.banana.camera;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ponkan.banana.R;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class CameraActivity extends AppCompatActivity implements
        CameraFragment.OnFragmentInteractionListener, EasyPermissions.PermissionCallbacks {

    public static final String TAG = "CameraActivity";

    private static final String[] CAMERA_PERMISSION =
            {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int RC_CAMERA_PERM = 123;

    private CameraFragment cameraFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraTask();

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
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    public void cameraTask() {
        if (hasCameraPermission()) {
            // Have permission, do the thing!
//            Toast.makeText(this, "TODO: Camera things", Toast.LENGTH_LONG).show();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera),
                    RC_CAMERA_PERM,
                    CAMERA_PERMISSION);
        }
    }

    private boolean hasCameraPermission() {
        return EasyPermissions.hasPermissions(this, CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
