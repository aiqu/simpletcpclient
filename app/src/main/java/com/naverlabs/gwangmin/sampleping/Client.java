package com.naverlabs.gwangmin.sampleping;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.*;
import java.nio.charset.*;

public class Client {

    public SocketChannel client = null;
    public InetSocketAddress isa = null;
    public RecvThread rt = null;
    public String remoteAddress = "localhost";
    public int remotePort = 8080;
    public Handler mHandler;

    public Client(String addr, int port, Handler handler) {
        remoteAddress = addr;
        remotePort = port;
        mHandler = handler;
        Log.d("GMLEE", "Set addr: "+remoteAddress+" and port: "+port);
    }

    public void connect() {
        try {
            client = SocketChannel.open();
            isa = new InetSocketAddress(remoteAddress, remotePort);
            client.connect(isa);
            client.configureBlocking(false);
            Log.d("GMLEE", "Connected");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finalize() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int sendMessage() {
        System.out.println("Inside SendMessage");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String msg = null;
        ByteBuffer bytebuf = ByteBuffer.allocate(1024);
        int nBytes = 0;
        try {
            msg = in.readLine();
            System.out.println("msg is " + msg);
            bytebuf = ByteBuffer.wrap(msg.getBytes());
            nBytes = client.write(bytebuf);
            System.out.println("nBytes is " + nBytes);
            if (msg.equals("quit") || msg.equals("shutdown")) {
                System.out.println("time to stop the client");
                interruptThread();
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                client.close();
                return -1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Wrote " + nBytes + " bytes to the server");
        return nBytes;
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
                        String result = charBuffer.toString() + cnt++;
                        Message msg = mHandler.obtainMessage(0, result);
                        mHandler.sendMessage(msg);
                        Log.d("GMLEE", "Received "+result);
                        buf.flip();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}