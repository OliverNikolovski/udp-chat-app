package client;

import model.Message;

import java.io.*;
import java.net.*;
import java.util.Objects;

public class ClientWriterThread extends Thread {

    private final Socket socket;
    private final ObjectOutputStream objectOutputStream;
    private String username;

    public ClientWriterThread(Socket socket) throws IOException {
        Objects.requireNonNull(socket);
        this.socket = socket;
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input;
        try {
            while ((input = br.readLine()) != null) {
                if (input.startsWith("login:")) {
                    this.username = input.split(":")[1];
                }
                Message message = new Message(this.username, input);
                this.objectOutputStream.writeObject(message);
                this.objectOutputStream.flush();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
