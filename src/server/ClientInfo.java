package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public class ClientInfo {
    private String username;
    private Socket socket;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public ClientInfo(String username, Socket socket) throws IOException {
        Objects.requireNonNull(username);
        Objects.requireNonNull(socket);
        this.username = username;
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream());
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public Socket getSocket() {
        return socket;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public String getUsername() {
        return username;
    }

    public void closeSocket() {
        printWriter.close();
        try {
            bufferedReader.close();
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
