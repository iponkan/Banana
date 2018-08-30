package com.ponkan.banana.util;

import android.os.Environment;

import java.io.File;

public class PathUtil {
    public static File getDir() {
        File file = new File(Environment.getExternalStorageDirectory(), "banana");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public static String getVideoPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/VID_20171122_101304.mp4";
    }

}
