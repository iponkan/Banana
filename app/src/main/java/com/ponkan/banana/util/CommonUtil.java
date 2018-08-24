package com.ponkan.banana.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class CommonUtil {

    //获取底部导航栏高度
    public static int getNavigationBarHeight(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                int appHeight = metrics.heightPixels;
                display.getRealMetrics(metrics);
                int logicalHeight = metrics.heightPixels;
                if (logicalHeight > appHeight) {
                    int height = logicalHeight - appHeight;
                    int navigationBarHeight = 0;
                    Resources resources = context.getResources();
                    int resourceId = resources.getIdentifier("navigation_bar_height", "dimen",
                            "android");
                    if (resourceId > 0) {
                        navigationBarHeight = resources.getDimensionPixelSize(resourceId);
                        return Math.min(navigationBarHeight, height);
                    }

                    return height;
                }
            }
        } catch (Throwable e) {
            //ignore
        }

        return 0;
    }
}
