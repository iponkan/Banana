package com.ponkan.banana.test;

import android.util.Log;

public class MyThread extends Thread {

    // 这里可以将run方法改为synchronized的
    @Override
    public synchronized void run() {
        while (true) {
            Log.d("MyThread", "before wait");
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("MyThread", "after wait");
        }

    }
}
