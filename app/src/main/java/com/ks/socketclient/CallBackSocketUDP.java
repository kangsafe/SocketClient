package com.ks.socketclient;

/**
 * Created by Admin on 2017/10/18 0018 14:22.
 * Author: kang
 * Email: kangsafe@163.com
 */
//UDP
public interface CallBackSocketUDP {
    void isConnect(boolean state);//判断状态
    void Receive(String data); //接收服务端返回的数据
}
