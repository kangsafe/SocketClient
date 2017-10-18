package com.ks.socketclient;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by Admin on 2017/10/18 0018 09:53.
 * Author: kang
 * Email: kangsafe@163.com
 */
public class ReceieveRunable implements Runnable {
    private final String TAG = getClass().getSimpleName();
    private InputStream inputStream;
    Socket mSocket;
    Handler handler;

    public ReceieveRunable(Socket socket, Handler handler) {
        this.mSocket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        while (!SocketService.stop) {
            try {
                if (mSocket != null && !mSocket.isClosed() && mSocket.isConnected()) {
                    inputStream = mSocket.getInputStream();
                    if (inputStream != null && inputStream.available() > 0) {
                        byte[] buf = new byte[inputStream.available()];
                        inputStream.read(buf);
                        String text = new String(buf);
                        Message msg = new Message();
                        msg.what = SocketService.SOCKET_RECEIEVE;
                        msg.obj = text;
                        handler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(SocketService.SOCKET_ERROR);
            }
        }
    }
}
