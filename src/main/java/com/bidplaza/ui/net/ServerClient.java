package com.bidplaza.ui.net;

import com.bidplaza.network.Message;
import com.bidplaza.network.ServerPort;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class ServerClient {
    private ServerClient() {}

    public static Message request(Message request) throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", ServerPort.get()), 3000);
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                out.writeObject(request);
                out.flush();
                return (Message) in.readObject();
            }
        }
    }
}
