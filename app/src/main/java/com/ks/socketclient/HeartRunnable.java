package com.ks.socketclient;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by Admin on 2017/10/18 0018 11:50.
 * Author: kang
 * Email: kangsafe@163.com
 */

public class HeartRunnable implements Runnable {
    private final String TAG = getClass().getSimpleName();
    private Socket mSocket;
    private Handler handler;

    public HeartRunnable(Socket socket, Handler handler) {
        this.handler = handler;
        this.mSocket = socket;
    }

    @Override
    public void run() {
        try {
            mSocket.sendUrgentData(0xFF);
        } catch (Exception e) {
            handler.sendEmptyMessage(SocketService.SOCKET_CLOSED);
            e.printStackTrace();
        }
    }
}
