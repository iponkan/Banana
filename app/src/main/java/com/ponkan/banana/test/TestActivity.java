package com.ponkan.banana.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.ponkan.banana.R;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button myButton;
    private Thread myThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        myButton = findViewById(R.id.bn_my);
        myButton.setOnClickListener(this);
        myThread = new MyThread();
        myThread.start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bn_my) {
            //First of all, I encourage you to really try hard to deal with concurrency issues
            // on a higher level of abstraction, i.e. solving it using classes from java.util.concurrent
            // such as ExecutorServices, Callables, Futures etc.
            //That being said, there's nothing wrong with synchronizing on a non-final field per se.
            // You just need to keep in mind that if the object reference changes,
            // the same section of code may be run in parallel. I.e., if one thread runs the code
            // in the synchronized block and someone calls setO(...), another thread can run
            // the same synchronized block on the same instance concurrently.
            //Synchronize on the object which you need exclusive access to
            // (or, better yet, an object "guarding" it).
            synchronized (myThread) {
                myThread.notify();
            }
        }
    }
}
