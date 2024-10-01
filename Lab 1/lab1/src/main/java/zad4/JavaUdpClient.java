package zad4;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class JavaUdpClient {
    static String trimZeros(String str) {
        int pos = str.indexOf(0);
        return pos == -1 ? str : str.substring(0, pos);
    }
    public static void main(String[] args) throws Exception
    {
        System.out.println("JAVA UDP CLIENT");
        DatagramSocket socket = null;
        int portNumber = 9008;

        try {
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            byte[] sendBuffer = "ping java".getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, portNumber);
            socket.send(sendPacket);

            byte[] resBuff = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(resBuff, resBuff.length);
            socket.receive(responsePacket);
            String responseMsg = new String(responsePacket.getData(), StandardCharsets.UTF_8);
            System.out.println("Response msg: "+trimZeros(responseMsg));
            SocketAddress socketAddress = responsePacket.getSocketAddress();
            System.out.println("server address: " + socketAddress);
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
