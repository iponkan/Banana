package com.ponkan.banana.camera.util;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class RotationUtil {
    public static int getRotation(@Nullable Activity context) {
        int rotation = Surface.ROTATION_0;//竖直方向
        WindowManager windowManager = null;
        if (context != null) {
            windowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        }
        Display display = null;
        if (windowManager != null) {
            display = windowManager.getDefaultDisplay();
        }
        if (display != null) {
            rotation = display.getRotation();
        }
        return rotation;
    }
}
