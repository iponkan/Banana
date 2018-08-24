package com.ponkan.banana.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ponkan.banana.BananaApplication;
import com.ponkan.banana.R;
import com.ponkan.banana.camera.util.CameraUtils;
import com.ponkan.banana.camera.widget.AspectFrameLayout;
import com.ponkan.banana.camera.widget.CameraRender;


import java.io.IOException;

import static android.content.Context.WINDOW_SERVICE;


/**
 * CameraFragment
 */
public class CameraFragment extends Fragment implements SurfaceTexture.OnFrameAvailableListener
        , CameraRender.OnSurfaceTextureListener {
    public static final String TAG = "CameraFragment";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private GLSurfaceView mCameraView;
    private Camera mCamera;
    private int mCameraPreviewWidth, mCameraPreviewHeight;
    private AspectFrameLayout mCameraViewContainer;
    private CameraRender mCameraRender;

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCameraView = view.findViewById(R.id.glsv_camera);
        mCameraViewContainer = view.findViewById(R.id.cameraPreview_afl);

        mCameraView.setEGLContextClientVersion(2);
        mCameraRender = new CameraRender(this);
        mCameraView.setRenderer(mCameraRender);
        mCameraView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCameraView.onResume();
        openCamera();
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseCamera();
        mCameraRender.pause();
        mCameraView.onPause();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            Log.d(TAG, "releaseCamera -- done");
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mCameraView.requestRender();
    }

    private void openCamera() {
        if (mCamera == null) {
            Camera.CameraInfo info = new Camera.CameraInfo();

            // Try to find a front-facing camera (e.g. for videoconferencing).
            int numCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numCameras; i++) {
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCamera = Camera.open(i);
                    break;
                }
            }

            if (mCamera == null) {
                Log.d(TAG, "No front-facing camera found; opening default");
                mCamera = Camera.open();    // opens first back-facing camera
            }
            if (mCamera == null) {
                throw new RuntimeException("Unable to open camera");
            }

            Camera.Parameters parms = mCamera.getParameters();

            CameraUtils.choosePreviewSize(parms, 1920, 1080);
            CameraUtils.chooseFixedPreviewFps(parms, 30);

            // Give the camera a hint that we're recording video.  This can have a big
            // impact on frame rate.
            parms.setRecordingHint(true);

            // leave the frame rate set to default
            mCamera.setParameters(parms);

            Camera.Size previewSize = parms.getPreviewSize();

            mCameraPreviewWidth = previewSize.width;
            mCameraPreviewHeight = previewSize.height;

            mCameraRender.setCameraPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);

            Display display = ((WindowManager) BananaApplication.getApplication().getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

            if (display.getRotation() == Surface.ROTATION_0) {
                mCamera.setDisplayOrientation(90);
                mCameraViewContainer.setAspectRatio((double) mCameraPreviewHeight / mCameraPreviewWidth);
            } else if (display.getRotation() == Surface.ROTATION_270) {
                mCameraViewContainer.setAspectRatio((double) mCameraPreviewHeight / mCameraPreviewWidth);
                mCamera.setDisplayOrientation(180);
            } else {
                // Set the preview aspect ratio.
                mCameraViewContainer.setAspectRatio((double) mCameraPreviewWidth / mCameraPreviewHeight);
            }

        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture st) {
        st.setOnFrameAvailableListener(this);
        try {
            mCamera.setPreviewTexture(st);//需要在startPreview之前调用
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        mCamera.startPreview();
    }
}
