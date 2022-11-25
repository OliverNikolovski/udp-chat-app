package client;

import java.io.IOException;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int serverPort = 4567;
        Socket socket = new Socket(serverAddress, serverPort);
        Thread t1 = new ClientReaderThread(socket);
        Thread t2 = new ClientWriterThread(socket);
        t1.start();
        t2.start();
    }
}
