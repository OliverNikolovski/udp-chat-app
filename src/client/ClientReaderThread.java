package client;

import dto.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Objects;

public class ClientReaderThread extends Thread {

    private final Socket socket;
    private final ObjectInputStream objectInputStream;

    public ClientReaderThread(Socket socket) throws IOException {
        Objects.requireNonNull(socket);
        this.socket = socket;
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        System.out.println("Client up and running...");
        try {
            while (!socket.isClosed()) {
                Message message = (Message) objectInputStream.readObject();
                System.out.println(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    public void closeEverything() {
        try {
            objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
