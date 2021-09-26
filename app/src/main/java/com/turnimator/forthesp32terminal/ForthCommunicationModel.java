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

    PrintWriter out = null;
    BufferedReader br = null;
    String host;
    int port;

    Thread connectThread, sendThread, receiveThread;

    public void connect(String h, int p) {
        host = h;
        port = p;

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
            return;
        }
        br = new BufferedReader(ir);
    }


    public void send(String text) {
        final String[] s = {""};
        Log.d("Send", text);
        if (!socket.isConnected() || out == null) {
            return;
        }
        out.println(text);
        out.flush();

    }

    public String receive() {
        String s = "";
        try {
            s = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }


    public boolean isConnected() {
        if (br == null) {
            return false;
        }
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