package com.ponkan.banana.camera.util;

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
}
