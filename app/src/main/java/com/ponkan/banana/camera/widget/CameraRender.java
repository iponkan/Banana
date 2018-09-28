package com.ponkan.banana.camera.widget;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.ponkan.banana.camera.ITakePicCallback;
import com.ponkan.banana.camera.record.TextureMovieEncoder;
import com.ponkan.banana.gles.Common;
import com.ponkan.banana.gles.FrameBufferObject;
import com.ponkan.banana.gles.FullFrameRect;
import com.ponkan.banana.gles.Texture2dProgram;

import java.io.File;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer {

    public static final String TAG = "CameraRender";
    private boolean mIncomingSizeUpdated;
    private int mRecordWidth;
    private int mRecordHeight;
    private float[] mSTMatrix = new float[16];

    private FullFrameRect mFullScreen;
    private int mTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private OnSurfaceTextureListener mOnSurfaceTextureListener;
    private boolean mFilterInit;
    private RecordManager mRecordManager;

    public CameraRender(OnSurfaceTextureListener listener) {
        mOnSurfaceTextureListener = listener;
        mRecordManager = new RecordManager(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mRecordManager.initRecordingEnabled();
        // Set up the texture blitter that will be used for on-screen display.  This
        // is *not* applied to the recording, because that uses a separate shader.
        mFullScreen = new FullFrameRect(
                new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT));

        mTextureId = mFullScreen.createTextureObject();

        // Create a SurfaceTexture, with an external texture, in this EGL context.  We don't
        // have a Looper in this thread -- GLSurfaceView doesn't create one -- so the frame
        // available messages will arrive on the main thread.
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mOnSurfaceTextureListener.onSurfaceTextureAvailable(mSurfaceTexture);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        Log.d(TAG, "onSurfaceChanged " + width + "x" + height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        mSurfaceTexture.updateTexImage();

        if (mRecordManager.mRecordingEnabled) {
            switch (mRecordManager.mRecordingStatus) {
                case RecordManager.RECORDING_OFF:
                    mRecordManager.startRecord();
                    break;
                case RecordManager.RECORDING_RESUMED:
                    mRecordManager.resumeRecord();
                    break;
                case RecordManager.RECORDING_ON:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordManager.mRecordingStatus);
            }
        } else {
            switch (mRecordManager.mRecordingStatus) {
                case RecordManager.RECORDING_ON:
                case RecordManager.RECORDING_RESUMED:
                    mRecordManager.stopRecord();
                    break;
                case RecordManager.RECORDING_OFF:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordManager.mRecordingStatus);
            }
        }

        mRecordManager.frameRecord(mTextureId, mSurfaceTexture);

        if (mRecordWidth <= 0 || mRecordHeight <= 0) {
            // Texture size isn't set yet.  This is only used for the filters, but to be
            // safe we can just skip drawing while we wait for the various races to resolve.
            // (This seems to happen if you toggle the screen off/on with power button.)
            Log.i(TAG, "Drawing before incoming texture size set; skipping");
            return;
        }

        if (!mFilterInit) {
            updateFilter();
            mFilterInit = true;
        }

        if (mIncomingSizeUpdated) {
            mFullScreen.getProgram().setTexSize(mRecordWidth, mRecordHeight);
            mIncomingSizeUpdated = false;
        }

        // Draw the video frame.
        mSurfaceTexture.getTransformMatrix(mSTMatrix);
        mFullScreen.drawFrame(mTextureId, mSTMatrix);
    }

    public void takePic(ITakePicCallback callback) {
        FrameBufferObject frameBufferObject = new FrameBufferObject();
        int bufferTexID;
        IntBuffer buffer;
        Bitmap bmp;

        bufferTexID = Common.genBlankTextureID(mRecordWidth, mRecordHeight);
        frameBufferObject.bindTexture(bufferTexID);
        GLES20.glViewport(0, 0, mRecordWidth, mRecordHeight);
//        mFrameRecorder.drawCache();
        buffer = IntBuffer.allocate(mRecordWidth * mRecordHeight);
        GLES20.glReadPixels(0, 0, mRecordWidth, mRecordHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        bmp = Bitmap.createBitmap(mRecordWidth, mRecordHeight, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buffer);
        Log.i(TAG, String.format("w: %d, h: %d", mRecordWidth, mRecordHeight));

        frameBufferObject.release();
        GLES20.glDeleteTextures(1, new int[]{bufferTexID}, 0);

        callback.onPicTaken(bmp);
    }

    public interface OnSurfaceTextureListener {
        void onSurfaceTextureAvailable(SurfaceTexture st);
    }

    public void pause() {
        if (mSurfaceTexture != null) {
            Log.d(TAG, "renderer pausing -- releasing SurfaceTexture");
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mFullScreen != null) {
            mFullScreen.release(false);     // assume the GLSurfaceView EGL context is about
            mFullScreen = null;             //  to be destroyed
        }
        mFilterInit = false;
    }

    public void setCameraPreviewSize(int width, int height) {
        Log.d(TAG, "setCameraPreviewSize");
        mRecordWidth = width;
        mRecordHeight = height;
        mIncomingSizeUpdated = true;
    }

    private void updateFilter() {
        Texture2dProgram.ProgramType programType;
        programType = Texture2dProgram.ProgramType.TEXTURE_EXT_BW;

        // Do we need a whole new program?  (We want to avoid doing this if we don't have
        // too -- compiling a program could be expensive.)
        if (programType != mFullScreen.getProgram().getProgramType()) {
            mFullScreen.changeProgram(new Texture2dProgram(programType));
            // If we created a new program, we need to initialize the texture width/height.
            mIncomingSizeUpdated = true;
        }
    }

    public void changeRecordingState(boolean bool, String savePath) {
        mRecordManager.changeRecordingState(bool, savePath);
    }

    /**
     * 录制控制
     */
    private static class RecordManager {

        private boolean mRecordingEnabled;
        private int mRecordingStatus;

        private static final int RECORDING_OFF = 0;
        private static final int RECORDING_ON = 1;
        private static final int RECORDING_RESUMED = 2;

        private CameraRender mCameraRender;
        private TextureMovieEncoder mVideoEncoder;//编码和混合器
        private File mOutputFile;

        RecordManager(CameraRender cameraRender) {
            mCameraRender = cameraRender;
            mVideoEncoder = new TextureMovieEncoder();
        }

        public void startRecord() {
            mVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
                    mOutputFile, mCameraRender.mRecordWidth, mCameraRender.mRecordHeight,
                    EGL14.eglGetCurrentContext()));
            mRecordingStatus = RecordManager.RECORDING_ON;
        }

        public void frameRecord(int mtextureid, SurfaceTexture surfaceTexture) {
            // Set the video encoder's texture name.  We only need to do this once, but in the
            // current implementation it has to happen after the video encoder is started, so
            // we just do it here.
            //
            // TODO: be less lame.
            mVideoEncoder.setTextureId(mtextureid);

            // Tell the video encoder thread that a new frame is available.
            // This will be ignored if we're not actually recording.
            mVideoEncoder.frameAvailable(surfaceTexture);//每一帧都传给录制器
        }

        public void resumeRecord() {
            mVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
            mRecordingStatus = RecordManager.RECORDING_ON;
        }

        public void stopRecord() {
            mVideoEncoder.stopRecording();
            mRecordingStatus = RecordManager.RECORDING_OFF;
        }

        public void initRecordingEnabled() {
            mRecordingEnabled = mVideoEncoder.isRecording();
            if (mRecordingEnabled) {
                mRecordingStatus = RECORDING_RESUMED;
            } else {
                mRecordingStatus = RECORDING_OFF;
            }
        }

        public void changeRecordingState(boolean bool, String savePath) {
            mRecordingEnabled = bool;
            if (bool) {
                mOutputFile = new File(savePath);
            }

        }
    }
}
