package com.naverlabs.gwangmin.sampleping;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.instacart.library.truetime.TrueTimeRx;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class MainActivity extends AppCompatActivity {

    Client c = null;
    TextView tvIp, tvPort, tvNtpport;
    Handler mHandler;
    Context mContext;
    String hostAddr;
    int hostPort, ntpPort;
    CameraView cameraView;
    Button recordingStatusBtn;
    static String GUID;
    boolean isConnected, isRecording;

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
        setContentView(R.layout.activity_main);
        mContext = this;
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case Client.CONNECTED:
                        Button btn = (Button)findViewById(R.id.connect);
                        btn.setText(R.string.disconnect);
                        isConnected = true;
                        break;
                    case Client.RECEIVED:
                        String result = (String)msg.obj;
                        if (result.contains("ping")) {
                            Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
                        } else if (result.contains("start")) {
                            startRecording();
                        } else if (result.contains("stop")) {
                            stopRecording();
                        } else {
                            Log.d("GMLEE", "Unhandled message: "+result);
                        }
                        break;
                    default:
                        Log.d("GMLEE", "Unexpected msg: "+ msg.toString());
                }
            }
        };
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        Log.d("GMLEE", "Permission granted");
        tvIp = findViewById(R.id.ip);
        tvPort = findViewById(R.id.port);
        tvNtpport = findViewById(R.id.ntpport);
        isRecording = false;
        isConnected = false;
        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    if (c != null) {
                        c.finalize();
                        c = null;
                    }
                    Button btn = (Button)v;
                    btn.setText(R.string.connect);
                    isConnected = false;
                } else {
                    updateAddress();
                    connect(hostAddr, hostPort);
                }
            }
        });
        findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAddress();
                sync();
            }
        });
        cameraView = findViewById(R.id.camera);
        recordingStatusBtn = findViewById(R.id.recording_status);
        recordingStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });
        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "External storage is not writable", Toast.LENGTH_LONG).show();
            finish();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void updateAddress() {
        hostAddr = tvIp.getText().toString();
        hostPort = Integer.parseInt(tvPort.getText().toString());
        ntpPort = Integer.parseInt(tvNtpport.getText().toString());
    }

    public void connect(String address, int port) {
        sync();
        if (c != null) {
            c.finalize();
        }
        c = new Client(address, port, mHandler);
        new Thread() {
            public void run() {
                c.connect();
                c.receiveMessage();
            }
        }.start();
    }

    public void sync() {
        Log.d("GMLEE", "SYNC");
        TrueTimeRx.build()
                .initializeRx(hostAddr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(date -> {
                    Log.d("GMLEE", "TrueTime was initialized and we have a time: " + date);
                    Toast.makeText(mContext, "Time synced", Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    public void startRecording() {
        if (!isRecording) {
            Log.d("GMLEE", "Start recording");
            long now = getUTCTime();
            File f = new File(Environment.getExternalStorageDirectory(), GUID + "_" + Long.toString(now) + ".mp4");
            Log.w("GMLEE", "Record video to " + f.getAbsolutePath());
            cameraView.captureVideo(f);
            String recordStr = getResources().getString(R.string.recording);
            recordingStatusBtn.setText(recordStr + " " + f.getName());
            isRecording = true;
        }
    }

    public void stopRecording() {
        if (isRecording) {
            Log.d("GMLEE", "Stop recording");
            cameraView.stopVideo();
            recordingStatusBtn.setText(R.string.startRecord);
            isRecording = false;
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public long getUTCTime() {
        if (TrueTimeRx.isInitialized()) {
            return TrueTimeRx.now().getTime();
        }
        return Calendar.getInstance().getTimeInMillis();
    }
}
