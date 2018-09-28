package com.ponkan.banana.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void show(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
