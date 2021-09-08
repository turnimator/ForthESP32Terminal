package com.turnimator.forthesp32terminal;
import android.util.Log;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

public class ForthCommunicationModel {
    static Socket socket = new Socket();
    EditText _view;
    static Thread receiver;
    static Thread sender;
    static Thread connector;
    static PrintWriter out = null;
    static  BufferedReader br = null;
    final static ArrayList<String> reply = new ArrayList<>();

    static String[] send(String host, int port, String text) {
    Log.d("ForthCommunicationModel", text);
        connector = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("ForthCommunicationModel", "run");
                try {
                    socket.connect(new InetSocketAddress(host, port));
                } catch (Exception e) {
                    reply.add(e.toString());
                    Log.d("Connection", e.toString());
                    return;
                }
                OutputStream os = null;
                try {
                    os = socket.getOutputStream();
                } catch (Exception e) {
                    reply.add(e.toString());
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
        });

        Log.d("ForthCommunicationModel", "Creating sender thread");
        sender = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("ForthCommuni...run", text);
                out.println(text);
                out.flush();
            }
        });

        receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                String s = "";
                reply.clear();
                while (s != null) {
                    try {
                        s = br.readLine();
                    } catch (Exception e) {
                        reply.add(e.toString());
                        return;
                    }
                    if (s != null) {
                        reply.add(s);
                        Log.i("RECV", s);
                    }
                }
            }
        });
        if ( ! socket.isConnected()) {
            connector.start();
            try {
                connector.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d("ForthCommunicationModel", "Starting receiver thread.");
        receiver.start();
        sender.start();

        try {
            sender.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            receiver.join(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String[] a = new String[reply.size()];
        reply.toArray(a);
        return a;
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
    public void connect(){
        connector.start();
    }
}