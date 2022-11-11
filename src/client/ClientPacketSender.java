package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class ClientPacketSender extends Thread {

    private final DatagramSocket socket;
    private final SocketAddress serverSocketAddress;
    private final ThreadManager threadManager;
    private String username;

    public ClientPacketSender(DatagramSocket socket, InetAddress serverAddress, int serverPort, ThreadManager threadManager) {
        this.socket = socket;
        this.serverSocketAddress = new InetSocketAddress(serverAddress, serverPort);
        this.threadManager = threadManager;
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String messageToSend;
        try {
            while ((messageToSend = br.readLine()) != null && !isInterrupted()) {
                DatagramPacket datagramPacket;
                byte[] buffer;
                // if the command is login, then send the command as is
                // if it's other command, then prepend the client's name to the command
                // for the server to be able to check if this client is logged in or not
                // and to be able to display the sender client's name at the receiver client's terminal when a message is sent
                if (messageToSend.startsWith("login:")) {
                    this.username = messageToSend.split(":")[1];
                    buffer = messageToSend.getBytes();
                    datagramPacket = new DatagramPacket(buffer, buffer.length, this.serverSocketAddress);
                    this.socket.send(datagramPacket);
                    continue;
                }
                buffer = (this.username + ":" + messageToSend).getBytes();
                datagramPacket = new DatagramPacket(buffer, buffer.length, this.serverSocketAddress);
                this.socket.send(datagramPacket);
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
            threadManager.stopThreads();
        }
    }

}
