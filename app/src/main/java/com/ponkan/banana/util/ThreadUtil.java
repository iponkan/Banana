package com.ponkan.banana.util;


public class ThreadUtil {


    public static void runOnThread(Runnable runnable) {
        if (runnable != null)
            new Thread(runnable).start();
    }
}
