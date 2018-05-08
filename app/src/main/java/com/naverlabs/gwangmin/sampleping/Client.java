package com.naverlabs.gwangmin.sampleping;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.instacart.library.truetime.TrueTimeRx;

import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import java.nio.charset.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class Client {

    public SocketChannel client = null;
    public InetSocketAddress isa = null;
    public RecvThread rt = null;
    public String remoteAddress = "localhost";
    public int remotePort = 8080;
    public Handler mHandler;
    public final static int CONNECTED = 0;
    public final static int RECEIVED = 1;

    public Client(String addr, int port, Handler handler) {
        remoteAddress = addr;
        remotePort = port;
        mHandler = handler;
        Log.d("GMLEE", "Set addr: "+remoteAddress+" and port: "+port);
    }

    public void connect() {
        try {
            if (client != null) {
                client.close();
            }
            client = SocketChannel.open();
            isa = new InetSocketAddress(remoteAddress, remotePort);
            client.connect(isa);
            client.configureBlocking(false);
            Log.d("GMLEE", "Connected");
            mHandler.sendEmptyMessage(CONNECTED);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finalize() {
        try {
            interruptThread();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage() {
        if (client != null) {
            rt = new RecvThread("Receive Thread", client);
            rt.start();
        }
    }

    public void interruptThread() {
        rt.val = false;
    }

    public class RecvThread extends Thread {

        public SocketChannel sc = null;
        public int cnt = 0;
        public boolean val = true;

        public RecvThread(String str, SocketChannel client) {
            super(str);
            sc = client;
        }

        public void run() {
            Log.d("GMLEE", "Inside receivemsg");
            ByteBuffer buf = ByteBuffer.allocate(2048);
            try {
                while (val) {
                    while (client.read(buf) > 0) {
                        buf.flip();
                        Charset charset = Charset.forName("us-ascii");
                        CharsetDecoder decoder = charset.newDecoder();
                        CharBuffer charBuffer = decoder.decode(buf);
                        String result = charBuffer.toString().trim() + cnt++;
                        Log.d("GMLEE", "Received "+result);
                        if (TrueTimeRx.isInitialized()) {
                            Log.d("GMLEE", "Initialized");
                            Date truetime = TrueTimeRx.now();
                            Date localtime = Calendar.getInstance().getTime();
                            long diffInMillies = truetime.getTime() - localtime.getTime();
                            Log.d("GMLEE", "TrueTime: " + truetime + " Diff: " + diffInMillies);
                            result += " time diff: " + diffInMillies;
                        }
                        Message msg = mHandler.obtainMessage(RECEIVED, result);
                        mHandler.sendMessage(msg);
                        buf.clear();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}