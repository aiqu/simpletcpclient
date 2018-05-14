package com.naverlabs.gwangmin.sampleping;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    static String GUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        GUID = pref.getString("ID", "");
        if (GUID.isEmpty()) {
            GUID = UUID.randomUUID().toString().substring(0,7);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("ID", GUID);
        }
        Log.d("GMLEE", "GUID: " + GUID);
        getSupportActionBar().setTitle(GUID);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
