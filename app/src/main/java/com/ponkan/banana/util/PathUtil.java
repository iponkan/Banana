package com.ponkan.banana.util;

import android.os.Environment;

import java.io.File;

public class PathUtil {

    private static final String APP_NAME = "Banana";

    private static String getPicName() {
        return APP_NAME + "_" + TimeSwitch.ToDate2(System.currentTimeMillis()) + "_save" + ".jpg";
    }

    private static String getVideoName() {
        return APP_NAME + "_" + TimeSwitch.ToDate2(System.currentTimeMillis()) + "_save" + ".mp4";
    }

    /**
     * 从编辑页处理的图片的路径
     */
    public static String getVideoSavePath() {
        String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_NAME;
        File file = new File(Environment.getExternalStorageDirectory(), APP_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return saveDir + "/" + getVideoName();
    }

    public static String getPicSavePath() {
        String saveDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + APP_NAME;
        File file = new File(Environment.getExternalStorageDirectory(), APP_NAME);
        if (!file.exists()) {
            file.mkdirs();
        }
        return saveDir + "/" + getPicName();
    }

}
