package com.ponkan.banana.camera.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 需要自己建立GL线程，支持旋转，平移等view的属性变化
 *
 * chrome过去使用TextureView作为合成表面，但我们出于几个原因切换到SurfaceView
 *
 * 1.由于invalidation和缓冲的特性，TextureView增加了额外1～3帧的延迟显示画面的更新
 * 2.TextureView总是使用GL合成，而SurfaceTexture可以使用硬件overlay后端，可以占用更少的内存带宽，消耗更少的能量
 * 3.TextureView的内部缓冲队列导致比SurfaceView使用更多的内存
 * 4.TextureView的动画和变换能力我们用不上
 *
 * 所以我们这里相机使用GLSurfaceView，播放器使用TextureView
 * 用GLSurfaceView开发起来较为方便
 *
 */
public class FilterTextureView extends TextureView implements TextureView.SurfaceTextureListener{

    public FilterTextureView(Context context) {
        super(context);
        init();
    }

    public FilterTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FilterTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FilterTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setSurfaceTextureListener(this);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}
