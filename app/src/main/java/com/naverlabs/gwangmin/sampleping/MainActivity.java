package com.naverlabs.gwangmin.sampleping;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Client c = null;
    TextView ip, port;
    Handler mHandler;
    Context mContext;

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
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipaddr = ip.getText().toString();
                int portval = Integer.parseInt(port.getText().toString());
                connect(ipaddr, portval);
            }
        });
    }

    public void connect(String address, int port) {
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
}
