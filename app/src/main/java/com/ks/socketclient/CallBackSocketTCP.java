package com.ks.socketclient;

/**
 * Created by Admin on 2017/10/18 0018 14:22.
 * Author: kang
 * Email: kangsafe@163.com
 */

//TCP
public interface CallBackSocketTCP {
    //这个回调用于获取服务端返回的数据
    void Receive(String info);
    //判断是否处在连接在状态
    void isConnect(boolean state);
}
