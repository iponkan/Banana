package com.ponkan.banana.test;

import android.util.Log;

public class MyThread extends Thread {

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
