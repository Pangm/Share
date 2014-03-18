package com.michelle.share.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Handler;
import android.util.Log;

public class ClientSocketHandler extends Thread {
	private static final String TAG = "ClientSocketHandler";
    private Handler handler;
    private ChatManager chat;
    private InetAddress mAddress;
    private int	port;

    public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress, int port) {
        this.handler = handler;
        this.mAddress = groupOwnerAddress;
        this.port = port;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
            		port), 5000);
            Log.d(TAG, "Launching the I/O handler");
            chat = new ChatManager(socket, handler);
            new Thread(chat).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public ChatManager getChat() {
        return chat;
    }
}
