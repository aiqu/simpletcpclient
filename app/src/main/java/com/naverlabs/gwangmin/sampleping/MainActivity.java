package com.naverlabs.gwangmin.sampleping;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.instacart.library.truetime.TrueTimeRx;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    Client c = null;
    TextView tvIp, tvPort, tvNtpport;
    Handler mHandler;
    Context mContext;
    String hostAddr;
    int hostPort, ntpPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                String result = (String)msg.obj;
                Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
            }
        };
        tvIp = findViewById(R.id.ip);
        tvPort = findViewById(R.id.port);
        tvNtpport = findViewById(R.id.ntpport);
        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAddress();
                connect(hostAddr, hostPort);
            }
        });
        findViewById(R.id.sync).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAddress();
                sync();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                c.interruptThread();
            }
        });
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
}
