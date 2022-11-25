package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientReaderThread extends Thread {

    private final Socket socket;
    private final BufferedReader bufferedReader;

    public ClientReaderThread(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        System.out.println("Client up and running...");
        try {
            String message;
            while (!socket.isClosed() && ((message = bufferedReader.readLine()) != null)) {
                if (message.startsWith("message:")) {
                    String[] parts = message.split(":");
                    String fromUser = parts[1];
                    message = fromUser + ": " + parts[2];
                }
                System.out.println(message);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    public void closeEverything() {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
