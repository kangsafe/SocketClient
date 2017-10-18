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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Admin on 2017/10/16 0016 13:07.
 * Author: kang
 * Email: kangsafe@163.com
 */

public class SocketService extends Service {
    private final String TAG = getClass().getSimpleName();
    public static LinkedList<byte[]> dats = new LinkedList<>();
    IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
    BootBroadcastReceiver receiver = new BootBroadcastReceiver();
    private int SERVICE_START_DELAYED = 5;
    public static final int SOCKET_RECEIEVE = 1;
    public static final int SOCKET_SEND = 2;
    public static final int SOCKET_CONNECTED = 0;
    public static final int SOCKET_CLOSED = -1;
    public static final int SOCKET_ERROR = -2;
    public static final int SOCKET_HEART = 3;
    private ScheduledExecutorService mExcutorService;//一个线程池
    //    String mHost = "114.55.74.183";
    String mHost = "192.168.1.127";
    int mPort = 9999;
    public static boolean stop = false;

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
        mExcutorService = Executors.newScheduledThreadPool(3);
        Connect();
        registerReceiver(receiver, filter);
        ScoketDemonService.startForeground(this);
        return START_STICKY;
    }

    private void init(Socket mSocket) {
        if (!isConnecting) {
            mExcutorService.scheduleWithFixedDelay(new HeartRunnable(mSocket, handler), 0, 10, TimeUnit.SECONDS);
            mExcutorService.execute(new ReceieveRunable(mSocket, handler));
            mExcutorService.execute(new SendRunable(mSocket, handler));
        }
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


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //已连接
                case SocketService.SOCKET_CONNECTED: {
                    Log.i(TAG, "已连接到服务器");
                    init((Socket) msg.obj);
                }
                break;
                //与服务器断开连接
                case SocketService.SOCKET_CLOSED: {
                    Log.i(TAG, "服务器已断开连接");
                    mExcutorService.shutdownNow();
                    Connect();
                }
                break;
                //发送数据
                case SocketService.SOCKET_SEND: {
                    Log.i(TAG, new String((byte[]) msg.obj));
                    break;
                }
                //接收数据
                case SOCKET_RECEIEVE: {
                    Log.i(TAG, "来自服务器的消息：" + msg.obj.toString());
                    MyApplication.exec(msg.obj.toString());
                }
                break;
                case SOCKET_ERROR: {
                    Log.i(TAG, "Socket 错误");
                }
                break;
                case SOCKET_HEART:
                    Log.i(TAG, "心跳包...");
                    break;
                default:
                    break;
            }
        }
    };
    private boolean isConnecting = false;

    private void Connect() {
        if (!isConnecting) {
            isConnecting = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //mHost为服务器地址，mPort和服务器的端口号一样
                        Socket mSocket = new Socket(mHost, mPort);
                        mSocket.setKeepAlive(true);
                        if (mSocket.isConnected()) {
                            Message msg = new Message();
                            msg.obj = mSocket;
                            msg.what = SocketService.SOCKET_CONNECTED;
                            handler.sendMessage(msg);
                        } else {
                            handler.sendEmptyMessage(SocketService.SOCKET_CLOSED);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.sendEmptyMessage(SocketService.SOCKET_CLOSED);
                    }
                    isConnecting = false;
                }
            }).start();
        }
    }
}
