package zad1;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.net.SocketAddress;

public class JavaUdpServer {
    static String trimZeros(String str) {
        int pos = str.indexOf(0);
        return pos == -1 ? str : str.substring(0, pos);
    }

    public static void main(String args[])
    {
        System.out.println("JAVA UDP SERVER");
        DatagramSocket socket = null;
        int portNumber = 9008;

        try{
            socket = new DatagramSocket(portNumber);
            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer = new byte[1024];

            while(true) {
                Arrays.fill(receiveBuffer, (byte)0);
                Arrays.fill(sendBuffer, (byte)0);
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);
                String msg = new String(receivePacket.getData());
                SocketAddress socketAddress = receivePacket.getSocketAddress();
                System.out.println("received msg: " + trimZeros(msg));
                System.out.println("sender's address: " + socketAddress);

                String response = "Pong from Java Server UDP";
                sendBuffer = response.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, socketAddress);
                socket.send(sendPacket);
                System.out.println("response sent");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
