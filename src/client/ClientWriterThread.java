package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Objects;

public class ClientWriterThread extends Thread {

    private final Socket socket;
    private final PrintWriter printWriter;
    private String username;

    public ClientWriterThread(Socket socket) throws IOException {
        Objects.requireNonNull(socket);
        this.socket = socket;
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String messageToSend;
        try {
            while ((messageToSend = br.readLine()) != null) {
                // if the command is login, then send the command as is
                // if it's other command, then prepend the client's name to the command
                // for the server to be able to check if this client is logged in or not
                // and to be able to display the sender client's name at the receiver client's terminal when a message is sent
                if (messageToSend.startsWith("login:")) {
                    this.username = messageToSend.split(":")[1];
                    this.printWriter.println(messageToSend);
                    continue;
                }
                messageToSend = this.username + ":" + messageToSend;
                this.printWriter.println(messageToSend);
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
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
