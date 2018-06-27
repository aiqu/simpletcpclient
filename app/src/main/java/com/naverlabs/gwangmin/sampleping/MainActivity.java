package com.naverlabs.gwangmin.sampleping;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;

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
            editor.commit();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_id:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final EditText editText = new EditText(this);
                builder.setTitle("Change ID")
                        .setView(editText)
                        .setPositiveButton("OK", (dialog, which) -> {
                            final String newID = editText.getText().toString();
                            if (!newID.isEmpty()) {
                                GUID = newID;
                                getPreferences(MODE_PRIVATE).edit().putString("ID", GUID).commit();
                                Log.d("GMLEE", "ID: " + GUID);
                                getSupportActionBar().setTitle(GUID);
                            }
                        })
                        .setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
