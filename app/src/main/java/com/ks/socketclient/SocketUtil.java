package com.ks.socketclient;

/**
 * Created by Admin on 2017/10/18 0018 14:23.
 * Author: kang
 * Email: kangsafe@163.com
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author jay chen
 *         Socket 工具类
 */
public class SocketUtil {
    Socket socket = null;

    public SocketUtil(Socket socket) {
        super();
        this.socket = socket;
    }

    //断开连接
    public void closeConnect() {
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //检测是否连接 如果断开就重连
    public boolean isConnect() {
        if (socket.isClosed()) {//检测是否关闭状态
            return false;
        }
        return true;
    }

    //发送数据
    public void sendData(String data) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(data.getBytes());
    }

    //接收数据
    public String receiveData() throws IOException {
        InputStream inputStream = socket.getInputStream();
//        DataInputStream data=new DataInputStream(inputStream);
        byte[] buf = new byte[1024];
        int len = inputStream.read(buf);
        String text = new String(buf, 0, len);
        return text;
    }
}
