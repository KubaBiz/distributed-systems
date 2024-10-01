package zadaniedomowe1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Arrays;

public class JavaTcpClient {

    private static final String Multicast_Address = "230.0.0.0";
    private static final int multicastPort = 12346;
    private static String nickname = null;

    public static void main(String[] args) throws IOException {
        System.out.println("JAVA TCP CLIENT");
        String NETWORK_INTERFACE_NAME = "lo";
        String hostName = "localhost";
        InetAddress address = InetAddress.getByName("localhost");
        String ASCII_ART = """
                      /`·.¸
                     /¸...¸`:·
                 ¸.·´  ¸   `·.¸.·´)
                : © ):´;      ¸  {
                 `·.¸ `·  ¸.·´\\`·¸)
                     `\\\\´´\\¸.·´
                """;
        int portNumber = 12345;
        Socket socket = null;
        DatagramSocket udpsocket = null;
        MulticastSocket multicastSocket = null;
        NetworkInterface networkInterface = null;

        try {
            // create sockets
            socket = new Socket(hostName, portNumber);
            udpsocket = new DatagramSocket(socket.getLocalPort());

            // in & out streams
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // create multicast socket
            multicastSocket = new MulticastSocket(null);
            multicastSocket.bind(new InetSocketAddress(multicastPort));

            networkInterface = NetworkInterface.getByName(NETWORK_INTERFACE_NAME);
            multicastSocket.joinGroup(new InetSocketAddress(Multicast_Address, multicastPort), networkInterface);

            System.out.println("Connected to server.");

            MulticastSocket finalMulticastSocket = multicastSocket;
            NetworkInterface finalNetworkInterface = networkInterface;
            Socket finalSocket = socket;
            DatagramSocket finalUdpsocket = udpsocket;
            //Multicast Thread
            Thread t1 = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (!Thread.interrupted()) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        finalMulticastSocket.receive(packet);

                        String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                        if (!receivedMessage.startsWith("[M] " + nickname)) {
                            System.out.println(receivedMessage);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error within Multicast");
                    try {
                        finalMulticastSocket.leaveGroup(new InetSocketAddress(Multicast_Address, multicastPort), finalNetworkInterface);
                    } catch (IOException ex) {
                        System.out.println("Thread 1: Already left multicast group");
                    }
                    finalMulticastSocket.close();
                    closeSocket(finalSocket, finalUdpsocket);
                }
            });
            t1.start();

            //TCP Thread
            Thread t2 = new Thread(() -> {
                try {
                    String messageFromServer;
                    while ((messageFromServer = in.readLine()) != null || !Thread.interrupted()) {
                        System.out.println(messageFromServer);
                    }
                } catch (IOException e) {
                    System.out.println("Error within TCP connection");
                    try {
                        finalMulticastSocket.leaveGroup(new InetSocketAddress(Multicast_Address, multicastPort), finalNetworkInterface);
                    } catch (IOException ex) {
                        System.out.println("Thread 2: Already left multicast group");
                    }
                    finalMulticastSocket.close();
                    closeSocket(finalSocket, finalUdpsocket);
                }
            });
            t2.start();

            //UDP Thread
            Thread t3 = new Thread(() -> {
                try {
                    byte[] receiveBuffer = new byte[1024];
                    while (!Thread.interrupted()) {
                        Arrays.fill(receiveBuffer, (byte) 0);

                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        finalUdpsocket.receive(receivePacket);

                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Error within UDP connection");
                    try {
                        finalMulticastSocket.leaveGroup(new InetSocketAddress(Multicast_Address, multicastPort), finalNetworkInterface);
                    } catch (IOException ex) {
                        System.out.println("Thread 3: Already left multicast group");
                    }
                    finalMulticastSocket.close();
                    closeSocket(finalSocket, finalUdpsocket);
                }
            });
            t3.start();

            // Pakiet "Hello"
            System.out.print("Enter nickname: ");
            String userInputLine = userInput.readLine();
            nickname = userInputLine;
            out.println(userInputLine);

            byte[] sendBuffer = "ping".getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
            udpsocket.send(sendPacket);
            //

            while (userInputLine != null) {
                System.out.println("Choose from: (C)hat, (U)DP, (M)ulticast, (Q)uit");
                userInputLine = userInput.readLine();
                if (userInputLine == null){
                    break;
                }
                switch (userInputLine.toLowerCase()) {
                    case "c" -> {
                        System.out.print("Enter TCP message: ");
                        userInputLine = userInput.readLine();
                        out.println(userInputLine);
                    }
                    case "u" -> {
                        System.out.println("Sending fish ASCII Art");
                        sendBuffer = ASCII_ART.getBytes();

                        sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
                        udpsocket.send(sendPacket);
                    }
                    case "m" -> {
                        System.out.print("Enter Multicast message: ");
                        String multicastMessage = userInput.readLine();
                        sendMulticastMessage("[M] "+nickname + ": " + multicastMessage);
                    }
                    case "q" -> {
                        multicastSocket.leaveGroup(new InetSocketAddress(Multicast_Address, multicastPort), networkInterface);
                        multicastSocket.close();
                        t1.interrupt();
                        t2.interrupt();
                        t3.interrupt();
                        closeSocket(socket, udpsocket);
                    }
                    default -> System.out.println("Wrong letter");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (multicastSocket != null) {
                multicastSocket.leaveGroup(new InetSocketAddress(Multicast_Address, multicastPort), networkInterface);
                multicastSocket.close();
            }
            closeSocket(socket, udpsocket);
        }
    }
    private static void closeSocket(Socket socket, DatagramSocket udpsocket) {
        try {
            if (socket != null) {
                socket.close();
                System.out.println("TCP Socket closed.");
            }
            if (udpsocket != null){
                udpsocket.close();
                System.out.println("UDP Socket closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static void sendMulticastMessage(String message) {
        try {
            InetAddress group = InetAddress.getByName(Multicast_Address);
            MulticastSocket multicastSocket = new MulticastSocket();

            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);
            multicastSocket.send(packet);

            multicastSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
