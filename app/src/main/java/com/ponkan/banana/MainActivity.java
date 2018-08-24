package com.ponkan.banana;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ponkan.banana.audio.AudioActivity;
import com.ponkan.banana.camera.CameraActivity;
import com.ponkan.banana.player.PlayerActivity;
import com.ponkan.banana.test.TestActivity;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
        , View.OnClickListener {

    public static final String TAG = "CameraActivity";

    private static final String[] CAMERA_PERMISSION =
            {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private static final int RC_CAMERA_PERM = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.bn_go_audio).setOnClickListener(this);
        findViewById(R.id.bn_go_camera).setOnClickListener(this);
        findViewById(R.id.bn_go_player).setOnClickListener(this);
        findViewById(R.id.bn_go_test).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bn_go_audio:
                audioTask();
                break;
            case R.id.bn_go_camera:
                cameraTask();
                break;
            case R.id.bn_go_player:
                playerTask();
                break;
            case R.id.bn_go_test:
                testTask();
                break;
        }
    }

    public void audioTask() {
        Intent intent = new Intent(this, AudioActivity.class);
        startActivity(intent);
    }

    public void playerTask() {
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    public void testTask() {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    public void cameraTask() {
        if (hasCameraPermission()) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
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
