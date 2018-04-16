package com.naverlabs.gwangmin.sampleping;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    Client c = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("GMLEE", "Start");
        c = new Client("10.42.0.1", 8080);
        new Thread() {
            public void run() {
                c.connect();
                c.receiveMessage();
            }
        }.start();
    }
}
