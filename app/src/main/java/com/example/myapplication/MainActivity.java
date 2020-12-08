package com.example.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      //  mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
       // setAmbientEnabled();
<<<<<<< HEAD
=======

>>>>>>> 83b50aecfbe905ffd6b87740409766b5bfc2e531
    }
}