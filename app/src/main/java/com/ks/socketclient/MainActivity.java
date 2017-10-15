package com.ks.socketclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Socket mSocket;
    String mHost = "192.168.0.102";
    int mPort = 2400;
    private final String TAG = "SocketClient";
    EditText textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.service).setOnClickListener(this);
        findViewById(R.id.send).setOnClickListener(this);
        textView = (EditText) findViewById(R.id.msg);
        //实例化线程池对象(一次执行所有线程)
        mExcutorService = Executors.newCachedThreadPool();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 4:
                    Log.i(TAG, msg.obj.toString());
                    dats.addLast(msg.obj.toString().getBytes());
                    break;
                case 3://
                    Log.i(TAG, "服务器已断开连接");
                    break;
                case 2://
                    Log.i(TAG, "已连接到服务器");
                    break;
                case 1://
                    Log.i(TAG, "来自服务器的消息：" + msg.obj.toString());
                    break;
            }
        }
    };

    //    List<byte[]> list = new ArrayList<>();
    LinkedList<byte[]> dats = new LinkedList<>();

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
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
                }).start();
                break;
            case R.id.send:
                if (!textView.getText().toString().isEmpty()) {
                    Message msg = new Message();
                    msg.what = 4;
                    msg.obj = textView.getText().toString();
                    handler.sendMessage(msg);
                }
                break;
        }

    }

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
                            String text = new String(buf, "GBK");
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
            try {
                while (true) {
                    if (mSocket == null || mSocket.isClosed() || !mSocket.isConnected()) {
                        reConnect();
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
