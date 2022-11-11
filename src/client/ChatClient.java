package client;

import java.net.*;

public class ChatClient {
    public static void main(String[] args) throws UnknownHostException, SocketException {
        InetAddress serverAddress = InetAddress.getByName("localhost");
        int serverPort = 4567;
        DatagramSocket datagramSocket = new DatagramSocket();
        ThreadManager threadManager = new ThreadManager();
        Thread t1 = new ClientPacketReceiver(datagramSocket, threadManager);
        Thread t2 = new ClientPacketSender(datagramSocket, serverAddress, serverPort, threadManager);
        threadManager.addThreads(t1, t2);
        t1.start();
        t2.start();
    }
}
