package com.ks.socketclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
//import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;
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
    //    String mHost = "192.168.1.116";
    int mPort = 9999;
    private final String TAG = getClass().getSimpleName();
    public static LinkedList<byte[]> dats = new LinkedList<>();
    IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
    BootBroadcastReceiver receiver = new BootBroadcastReceiver();
    private int SERVICE_START_DELAYED = 5;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cancelAutoStartService(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //实例化线程池对象(一次执行所有线程)
        mExcutorService = Executors.newCachedThreadPool();
        mExcutorService.execute(new ConnectRunnable());
        mExcutorService.execute(new ReceieveRunable());
        mExcutorService.execute(new SendRunable());
        registerReceiver(receiver, filter);
        ScoketDemonService.startForeground(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "发送广播");
//        sendBroadcast(new Intent("com.ks.socketclient.receiver"));
        BootBroadcastReceiver.cancelAlarm(this);
        unregisterReceiver(receiver);
        mExcutorService.shutdownNow();
        startServiceAfterClosed(this, SERVICE_START_DELAYED);//5s后重启
    }

    /**
     * service停掉后自动启动应用
     *
     * @param context
     * @param delayed 延后启动的时间，单位为秒
     */
    private static void startServiceAfterClosed(Context context, int delayed) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayed * 1000, getOperation(context));
    }

    public static void cancelAutoStartService(Context context) {
        AlarmManager alarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(getOperation(context));
    }

    private static PendingIntent getOperation(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        PendingIntent operation = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return operation;
    }


    private Handler handler = new Handler() {
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
//                    Toast.makeText(SocketService.this, "已连接到服务器", Toast.LENGTH_LONG).show();
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

        public ReceieveRunable() {

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

    class SendRunable implements Runnable {
        private OutputStream out;//用来写入信息发给客户端

        public SendRunable() {

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
                        Thread.sleep(2000);
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
                Log.i(TAG, "心跳包...");
                try {
                    mSocket.sendUrgentData(0xFF);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "服务器已断开连接");
                    reConnect();
                }
                try {
                    Random random = new Random();
                    int num = random.nextInt(240);
                    Thread.sleep(num * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void reConnect() {
            try {
                //mHost为服务器地址，mPort和服务器的端口号一样
                mSocket = new Socket(mHost, mPort);
                mSocket.setKeepAlive(true);
                if (mSocket.isConnected()) {
                    handler.sendEmptyMessage(2);
                } else {
                    handler.sendEmptyMessage(3);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
