package com.ponkan.banana.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ponkan.banana.R;
import com.ponkan.banana.camera.util.CameraUtils;
import com.ponkan.banana.camera.util.ImageUtil;
import com.ponkan.banana.camera.util.RotationUtil;
import com.ponkan.banana.camera.widget.AspectFrameLayout;
import com.ponkan.banana.camera.widget.CameraRender;
import com.ponkan.banana.camera.widget.SegmentBar;
import com.ponkan.banana.camera.widget.ModeView;
import com.ponkan.banana.player.PlayerActivity;
import com.ponkan.banana.util.CommonUtil;
import com.ponkan.banana.util.PathUtil;
import com.ponkan.banana.util.ThreadUtil;
import com.ponkan.banana.util.ToastUtil;


import java.io.IOException;


/**
 * CameraFragment
 */
public class CameraFragment extends Fragment implements SurfaceTexture.OnFrameAvailableListener
        , CameraRender.OnSurfaceTextureListener, View.OnClickListener, SegmentBar.ITakeController,
        ModeView.ModeChangeListener, ITakePicCallback {
    public static final String TAG = "CameraFragment";

    private GLSurfaceView mCameraView;
    private Camera mCamera;
    private int mCameraPreviewWidth, mCameraPreviewHeight;
    private AspectFrameLayout mCameraViewContainer;
    private CameraRender mCameraRender;
    private ImageView mIvTakePic;
    private ImageView mIvDelete;
    private ImageView mIvConfirm;
    private int mRotation = Surface.ROTATION_90;//默认为竖直方向
    private String mVideoSavePath;
    private Handler mHandler;
    private SegmentBar mSegmentBar;
    private int mMode;
    private ModeView mModeView;
    private OnFragmentInteractionListener mOnFragmentInteractionListener;

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
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
        mSegmentBar = view.findViewById(R.id.sb_take_video);
        mSegmentBar.setITakeController(this);

        mCameraView.setEGLContextClientVersion(2);
        mCameraRender = new CameraRender(this);
        mCameraView.setRenderer(mCameraRender);
        mCameraView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        ViewGroup viewGroup = view.findViewById(R.id.fl_bottom_control);
        int height = CommonUtil.getNavigationBarHeight(getContext());
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewGroup.getLayoutParams();
        layoutParams.bottomMargin = height;

        mIvTakePic = view.findViewById(R.id.iv_take_pic);
        mIvTakePic.setOnClickListener(this);
        mIvDelete = view.findViewById(R.id.iv_delete);
        mIvDelete.setOnClickListener(this);
        mIvConfirm = view.findViewById(R.id.iv_confirm);
        mIvConfirm.setOnClickListener(this);

        mModeView = view.findViewById(R.id.mv_mode);
        mModeView.setModeChangeListener(this);
        mMode = mModeView.getMode();

    }

    @Override
    public void onModeChange(int newMode) {
        mMode = newMode;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.iv_take_pic:
                if (mMode == ModeView.MODE_PIC) {
                    takePic();
                } else if (mMode == ModeView.MODE_VID) {
                    record();
                }

                break;
            case R.id.iv_delete:
//                mSegmentBar.cancelLastSection();
                break;
            case R.id.iv_confirm:
//                goToPlayer();
                break;
        }
    }

    private void record() {
        if (mIvTakePic.isSelected()) {
            mIvTakePic.setSelected(false);
            mCameraView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mCameraRender.changeRecordingState(false, null);
                    goToPlayer();
                }
            });
        } else {
            mIvTakePic.setSelected(true);
            mCameraView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    mVideoSavePath = PathUtil.getVideoSavePath();
                    mCameraRender.changeRecordingState(true, mVideoSavePath);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSegmentBar.startNewSection();
                        }
                    });
                }
            });
        }
    }

    private void takePic() {
        mCameraView.queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraRender.takePic(CameraFragment.this);
            }
        });
    }

    private void goToPlayer() {
        // TODO: 2018/8/30 需要release的时间，这里先延时跳转
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getContext(), PlayerActivity.class);

                String[] uris = new String[1];
                uris[0] = mVideoSavePath;
                intent.putExtra(PlayerActivity.URI_LIST_EXTRA, uris);
                intent.setAction(PlayerActivity.ACTION_VIEW_LIST);
                startActivity(intent);
            }
        }, 1000);

    }

    @Override
    public void onStart() {
        super.onStart();
        mCameraView.onResume();
        openCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mRotation == Surface.ROTATION_90 | mRotation == Surface.ROTATION_270) {
                    mCameraRender.setCameraPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);
                } else {
                    mCameraRender.setCameraPreviewSize(mCameraPreviewHeight, mCameraPreviewWidth);
                }

            }
        });
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mOnFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnFragmentInteractionListener = null;
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

            // 相机的输出比例和我们View的比例不一定一样，
            // 需要通过改变View的大小和裁剪预览数据来适配

            mCameraRender.setCameraPreviewSize(mCameraPreviewWidth, mCameraPreviewHeight);

            mRotation = RotationUtil.getRotation(getActivity());

            if (mRotation == Surface.ROTATION_0) {
                mCamera.setDisplayOrientation(90);
                mCameraViewContainer.setAspectRatio((double) mCameraPreviewHeight / mCameraPreviewWidth);
            } else if (mRotation == Surface.ROTATION_270) {
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

    @Override
    public void leastLimitMatch() {

    }

    @Override
    public void leastLimitUnMatch() {

    }

    @Override
    public void takenOver() {

    }

    @Override
    public boolean deleteLastSection() {
        return false;
    }

    @Override
    public void takenTimeCallback(int time) {

    }

    @Override
    public void deleteStateChange(int state) {

    }

    @Override
    public void deleteLastSectionFailed() {

    }

    @Override
    public void onPicTaken(final Bitmap bitmap) {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                final String path = ImageUtil.saveBitmap(bitmap);
                if (null != path) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.show(getContext(), "图片保存成功，路径为：" + path);
                            mOnFragmentInteractionListener.go2ImagePreview(path);
                        }
                    });
                }
            }
        });
    }
}
