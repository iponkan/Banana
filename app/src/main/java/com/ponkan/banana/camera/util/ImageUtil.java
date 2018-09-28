package com.ponkan.banana.camera.util;

import android.graphics.Bitmap;
import android.util.Log;

import com.ponkan.banana.util.PathUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {


    private static final String LOG_TAG = "ImageUtil";


    public static String saveBitmap(Bitmap bmp) {
        String path = PathUtil.getPicSavePath();
        return saveBitmap(bmp, path);
    }

    private static String saveBitmap(Bitmap bmp, String filename) {

        Log.i(LOG_TAG, "saving Bitmap : " + filename);

        try {
            FileOutputStream fileout = new FileOutputStream(filename);
            BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileout);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bufferOutStream);
            bufferOutStream.flush();
            bufferOutStream.close();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Err when saving bitmap...");
            e.printStackTrace();
            return null;
        }

        Log.i(LOG_TAG, "Bitmap " + filename + " saved!");
        return filename;
    }
}