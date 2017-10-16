package com.ks.socketclient;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.View;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 作者: 韩大发.
 * 邮箱: handafa@126.com
 * 时间: 15/12/17 19:49
 * 描述:
 * 版本:1.0
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initRoot(this);
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
        }

    // 获取ROOT权限
    public static void initRoot(Context context) {
        if (is_root()) {
            Log.i("root", "已经获取root权限");
            try {
                Process process = Runtime.getRuntime().exec("su");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                upgradeRootPermission(context.getPackageCodePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 判断是否具有ROOT权限
    public static boolean is_root() {
        boolean res = false;
        try {
            if ((!new File("/system/bin/su").exists()) &&
                    (!new File("/system/xbin/su").exists())) {
                res = false;
            } else {
                res = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static void hibernate() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(
                    process.getOutputStream());
            out.writeBytes("echo mem > /sys/power/state \n");
            out.writeBytes("exit\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(
                    process.getOutputStream());
            out.writeBytes("reboot \n");
            out.writeBytes("exit\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void wakeup() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(
                    process.getOutputStream());
            out.writeBytes("echo on > /sys/power/state \n");
            out.writeBytes("exit\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(
                    process.getOutputStream());
            out.writeBytes("reboot -p\n");
            out.writeBytes("exit\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exec(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream out = new DataOutputStream(
                    process.getOutputStream());
            out.writeBytes(cmd + "\n");
            out.writeBytes("exit\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
