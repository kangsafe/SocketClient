package com.ks.socketclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Admin on 2017/6/15 0015 16:11.
 * Author: kang
 * Email: kangsafe@163.com
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
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
        }
    }
}
