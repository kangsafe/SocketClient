package com.ks.socketclient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.util.Log;

/**
 * Created by Admin on 2017/6/15 0015 16:11.
 * Author: kang
 * Email: kangsafe@163.com
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_HEALTH_CHECK = "com.mpush.HEALTH_CHECK";
    public static final String ACTION_NOTIFY_CANCEL = "com.mpush.NOTIFY_CANCEL";
    public static int delay = 240000;
    public static NetworkInfo.State STATE = NetworkInfo.State.UNKNOWN;

    //重写onReceive方法
    @Override
    public void onReceive(Context context, Intent intent) {
        //后边的XXX.class就是要启动的服务
//        Intent service = new Intent(context, UsbPrintActivity.class);
//        context.startService(service);
//        Log.v("TAG", "开机自动服务自动启动.....");
        //启动应用，参数为需要自动启动的应用的包名
//        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
//        context.startActivity(intent);
//        if (intent.getAction().equals(RECEIVE_BOOT_COMPLETED)) {
//            context.startActivity(new Intent(context, UsbPrintActivity.class));
//        }
        Log.d("SocketService", "BootReceiver.onReceive: " + intent.getAction());
        System.out.println("自启动程序即将执行");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED") ||
                intent.getAction().equals(Intent.ACTION_TIME_TICK) ||
                intent.getAction().equals("com.ks.socketclient.receiver")) {
            if (!MyApplication.isServiceWork(context, "com.ks.socketclient.SocketService")) {
                Intent mBootIntent = new Intent(context, SocketService.class);
                context.startService(mBootIntent);
            }
        } else if (ACTION_HEALTH_CHECK.equals(intent.getAction())) {//处理心跳
            startAlarm(context, delay);
        }
    }

    static void startAlarm(Context context, int delay) {
        Intent it = new Intent(BootBroadcastReceiver.ACTION_HEALTH_CHECK);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pi);
        BootBroadcastReceiver.delay = delay;
    }

    static void cancelAlarm(Context context) {
        Intent it = new Intent(BootBroadcastReceiver.ACTION_HEALTH_CHECK);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    public static boolean hasNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }
}
