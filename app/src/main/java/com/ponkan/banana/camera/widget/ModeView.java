package com.ponkan.banana.camera.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ponkan.banana.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ModeView extends RelativeLayout implements View.OnClickListener {

    public static final int MODE_PIC = 0;
    public static final int MODE_VID = 1;

    @IntDef({MODE_PIC, MODE_VID})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    @Mode
    private int mMode = MODE_PIC;

    private TextView mTvPic;
    private TextView mTvVid;

    public ModeView(Context context) {
        super(context);
        init(context);
    }

    public ModeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ModeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.mode_layout, this);
        mTvPic = findViewById(R.id.tv_pic);
        mTvPic.setOnClickListener(this);
        mTvVid = findViewById(R.id.tv_vid);
        mTvVid.setOnClickListener(this);
    }


    @Mode
    public int getMode() {
        return mMode;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tv_pic:
                if (mMode != MODE_PIC) {
                    mTvPic.setTextColor(Color.WHITE);
                    mTvVid.setTextColor(Color.parseColor("#80ffffff"));
                    mMode = MODE_PIC;
                    if (mModeChangeListener != null) {
                        mModeChangeListener.onModeChange(mMode);
                    }
                }
                break;
            case R.id.tv_vid:
                if (mMode != MODE_VID) {
                    mTvVid.setTextColor(Color.WHITE);
                    mTvPic.setTextColor(Color.parseColor("#80ffffff"));
                    mMode = MODE_VID;
                    if (mModeChangeListener != null) {
                        mModeChangeListener.onModeChange(mMode);
                    }
                }
                break;
        }
    }

    private ModeChangeListener mModeChangeListener;

    public void setModeChangeListener(ModeChangeListener l) {
        mModeChangeListener = l;
    }

    public interface ModeChangeListener {
        void onModeChange(int newMode);
    }
}
