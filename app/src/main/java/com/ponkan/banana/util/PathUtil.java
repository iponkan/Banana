package com.ponkan.banana.util;

import android.os.Environment;

import java.io.File;

public class PathUtil {

    private static final String APP_NAME = "Banana";


    public static String getName() {
        return APP_NAME + "_" + TimeSwitch.ToDate2(System.currentTimeMillis()) + "_save" + ".mp4";
    }

    /**
     * 从编辑页处理的图片的路径
     *
     * @return
     */
    public static String getVideoSavePath() {
        String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_NAME;
        File file = new File(Environment.getExternalStorageDirectory(), APP_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return saveDir + "/" + getName();
    }

    public static String getVideoPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/VID_20171122_101304.mp4";
    }

}
