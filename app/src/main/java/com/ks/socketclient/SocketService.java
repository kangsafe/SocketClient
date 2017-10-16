package com.ks.socketclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Admin on 2017/10/16 0016 13:07.
 * Author: kang
 * Email: kangsafe@163.com
 */

public class SocketService extends Service {
    Socket mSocket;
    String mHost = "114.55.74.183";
    int mPort = 2400;
    private final String TAG = getClass().getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //实例化线程池对象(一次执行所有线程)
        mExcutorService = Executors.newCachedThreadPool();
        try {
            mExcutorService.execute(new ConnectRunnable());
            mExcutorService.execute(new ReceieveRunable());
            mExcutorService.execute(new SendRunable());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    LinkedList<byte[]> dats = new LinkedList<>();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 4:
                    Log.i(TAG, msg.obj.toString());
                    try {
                        dats.addLast(msg.obj.toString().getBytes("GB2312"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3://
                    Log.i(TAG, "服务器已断开连接");
                    break;
                case 2://
                    Log.i(TAG, "已连接到服务器");
                    break;
                case 1://
                    Log.i(TAG, "来自服务器的消息：" + msg.obj.toString());
                    MyApplication.exec(msg.obj.toString());
                    break;
            }
        }
    };

    private ExecutorService mExcutorService;//一个线程池

    class ReceieveRunable implements Runnable {
        private InputStream inputStream;

        public ReceieveRunable() throws IOException {

        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (mSocket != null && !mSocket.isClosed() && mSocket.isConnected()) {
                        inputStream = mSocket.getInputStream();
                        if (inputStream != null && inputStream.available() > 0) {
                            byte[] buf = new byte[inputStream.available()];
                            inputStream.read(buf);
                            String text = new String(buf);
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = text;
                            handler.sendMessage(msg);
                        }
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "接收数据断开");
            }
        }
    }

    private long tm = 0;

    private void reConnect() throws IOException {
        if (System.currentTimeMillis() - tm < 1000) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //mHost为服务器地址，mPort和服务器的端口号一样
            mSocket = new Socket(mHost, mPort);
            mSocket.setKeepAlive(true);
            if (mSocket.isConnected()) {
                handler.sendEmptyMessage(2);
            } else {
                handler.sendEmptyMessage(3);
            }
        }
        tm = System.currentTimeMillis();
    }

    class SendRunable implements Runnable {
        private OutputStream out;//用来写入信息发给客户端

        public SendRunable() throws IOException {

        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (mSocket != null && !mSocket.isClosed() && mSocket.isConnected() && dats != null && dats.size() > 0) {
                        out = mSocket.getOutputStream();
                        out.write(dats.removeFirst());
                        out.flush();
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

            }
        }
    }

    class ConnectRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(10000);
                    mSocket.sendUrgentData(0xFF);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.i(TAG, "服务器已断开连接");
                    try {
                        reConnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
