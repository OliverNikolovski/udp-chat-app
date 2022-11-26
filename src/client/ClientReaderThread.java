package client;

import model.Client;
import model.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

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
                Object data = objectInputStream.readObject();
                if (Message.class.isAssignableFrom(data.getClass())) { // message is send
                    Message message = (Message) data;
                    System.out.println(message);
                }
                else {  // array of logged in clients is sent
                    Client[] clients = (Client[]) data;
                    String joinedClientUsernames =
                            Arrays.stream(clients)
                            .reduce(new StringJoiner(", "),
                                    (stringJoiner, client) -> stringJoiner.add(client.getUsername()),
                                    StringJoiner::merge)
                            .toString();
                    System.out.println(joinedClientUsernames);
                }
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
