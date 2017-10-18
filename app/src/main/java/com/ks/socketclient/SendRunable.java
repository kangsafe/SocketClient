package com.ks.socketclient;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.ks.socketclient.SocketService.dats;

/**
 * Created by Admin on 2017/10/18 0018 11:44.
 * Author: kang
 * Email: kangsafe@163.com
 */

public class SendRunable implements Runnable {
    private OutputStream out;//用来写入信息发给客户端
    private Socket mSocket;
    private Handler handler;

    public SendRunable(Socket socket, Handler handler) {
        this.mSocket = socket;
        this.handler = handler;
    }

    @Override
    public void run() {
        while (!SocketService.stop) {
            try {
                if (mSocket != null && !mSocket.isClosed() && mSocket.isConnected() && dats != null && dats.size() > 0) {
                    out = mSocket.getOutputStream();
                    byte[] datas = dats.getFirst();
                    out.write(datas);
                    out.flush();
                    dats.removeFirst();
                    Message msg = new Message();
                    msg.what = SocketService.SOCKET_SEND;
                    msg.obj = datas;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(SocketService.SOCKET_ERROR);
            } finally {
            }
        }
    }
}
