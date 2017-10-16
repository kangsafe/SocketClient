package com.ks.socketclient;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ks.socketclient.MyApplication.isServiceWork;

public class MainActivity extends Activity implements View.OnClickListener {
    EditText textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.service).setOnClickListener(this);
        findViewById(R.id.send).setOnClickListener(this);
        textView = (EditText) findViewById(R.id.msg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                if (!isServiceWork(this, "com.ks.socketclient.SocketService")) {
                    Intent intent = new Intent(this, SocketService.class);
                    startService(intent);
                } else {
                    Toast.makeText(this, "服务已经在运行", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.send:
                String str = textView.getText().toString();
                if (!str.isEmpty()) {
//                    Message msg = new Message();
//                    msg.what = 4;
//                    msg.obj = textView.getText().toString().trim();
                    SocketService.dats.add(str.getBytes());
                }
                break;
        }

    }
}
