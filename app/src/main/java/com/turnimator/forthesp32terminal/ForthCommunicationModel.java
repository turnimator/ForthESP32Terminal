package com.turnimator.forthesp32terminal;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ForthCommunicationModel {
    Socket socket = new Socket();
    boolean canSend = true;
    PrintWriter out = null;
    BufferedReader br = null;
    String host;
    int port;

    Thread connectThread, sendThread, receiveThread;


    public void connect(String h, int p) {
        host = h;
        port = p;
        connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.connect(new InetSocketAddress(host, port));
                } catch (Exception e) {
                    Log.d("Connection", e.toString());
                    return;
                }
                OutputStream os = null;
                try {
                    os = socket.getOutputStream();
                } catch (Exception e) {
                    Log.d("Connection stream", e.toString());
                    return;
                }
                out = new PrintWriter(os);

                InputStreamReader ir = null;
                try {
                    ir = new InputStreamReader(socket.getInputStream());
                } catch (Exception e) {
                    Log.d("Connection inputStream", e.toString());
                }
                br = new BufferedReader(ir);

            }
        });
        connectThread.start();
    }

    public synchronized void send(String text) {
        final String[] s = {""};
        Log.d("Send", text);
        if ( ! socket.isConnected()){
            connect(host, port);
        }
        while (!canSend) {
            try {
                wait();
            } catch (InterruptedException ex) {

            }
        }
        canSend = false;
        out.println(text);
        out.flush();

    }

    public synchronized String receive() {
        final String[] s = {null};
        while(canSend){
            try {
                wait();
            } catch(InterruptedException ex){

            }
        }
        try {
            s[0] = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s[0];
    }

    public boolean isConnected() {
        return socket.isConnected();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}