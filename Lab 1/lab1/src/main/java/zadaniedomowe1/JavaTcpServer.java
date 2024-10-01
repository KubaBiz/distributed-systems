package zadaniedomowe1;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class JavaTcpServer {

    public static void main(String[] args) throws IOException {

        System.out.println("JAVA TCP SERVER");
        int portNumber = 12345;
        ServerSocket serverSocket = null;
        DatagramSocket udpSocket = null;
        ArrayList<ClientHandlerTCP> TCP_connections = new ArrayList<>();
        ArrayList<InetSocketAddress> clientAddresses = new ArrayList<>();

        try {
            // create socket
            serverSocket = new ServerSocket(portNumber);
            udpSocket = new DatagramSocket(portNumber);

            DatagramSocket finalUdpSocket = udpSocket;
            ServerSocket finalServerSocket = serverSocket;
            new Thread(() -> {
                try {
                    byte[] receiveBuffer = new byte[1024];
                    while (true) {
                        Arrays.fill(receiveBuffer, (byte) 0);

                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        finalUdpSocket.receive(receivePacket);

                        InetSocketAddress clientAddress = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());
                        if (!clientAddresses.contains(clientAddress)){
                            clientAddresses.add(clientAddress);
                        }

                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("UDP Message from " + TCP_connections.get(clientAddresses.indexOf(clientAddress)).nickname + ": \n" + message);

                        // Broadcast message to all clients except the sender
                        if (!message.equalsIgnoreCase("ping")){
                            byte[] sendData = ("[UDP] " + TCP_connections.get(clientAddresses.indexOf(clientAddress)).nickname + ": \n" + message).getBytes();
                            for (InetSocketAddress client : clientAddresses) {
                                if (!clientAddress.equals(client)) {
                                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, client.getAddress(), client.getPort());
                                    finalUdpSocket.send(sendPacket);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error within UDP");
                    e.printStackTrace();
                } finally {
                    try {
                        finalServerSocket.close();
                    } catch (IOException e) {
                        System.out.println("UDP Thread: Server Socket already closed");
                    }
                    finalUdpSocket.close();
                }

            }).start();

            while(true){

                // accept client
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandlerTCP clientHandler = new ClientHandlerTCP(clientSocket, TCP_connections, clientAddresses);
                TCP_connections.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Error within main server thread");
        }
        finally{
            if (serverSocket != null){
                serverSocket.close();
            }
            if (udpSocket != null){
                udpSocket.close();
            }
            System.exit(0);
        }
    }

}
