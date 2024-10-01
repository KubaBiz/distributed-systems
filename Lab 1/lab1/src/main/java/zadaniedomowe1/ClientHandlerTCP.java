package zadaniedomowe1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandlerTCP implements  Runnable{
        private final Socket clientSocket;
        private PrintWriter out;

        private ArrayList<ClientHandlerTCP> TCP_clients;
        private ArrayList<InetSocketAddress> addresses;

        public String nickname;

        public ClientHandlerTCP(Socket socket, ArrayList<ClientHandlerTCP> TCP_clients, ArrayList<InetSocketAddress> addresses) {
            this.clientSocket = socket;
            this.TCP_clients = TCP_clients;
            this.addresses = addresses;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                this.nickname = in.readLine();

                System.out.println(nickname + " joined chat");

                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    System.out.println("Received message from " + nickname + ": " + message);
                    broadcastMessage(message);
                }
            } catch (IOException e) {
                System.out.println("Error within TCP Client Thread");
            } finally {
                try {
                    clientSocket.close();
                    if (addresses.size() == TCP_clients.size()) {
                        addresses.remove(TCP_clients.indexOf(this));
                    }
                    TCP_clients.remove(this);
                    System.out.println("Client " + nickname + " disconnected: " + clientSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }        }

        private void broadcastMessage(String message) {
            for (ClientHandlerTCP client : TCP_clients) {
                if (client != this) {
                    client.sendMessage("[TCP] " + nickname + ": " + message);
                }
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }

}
