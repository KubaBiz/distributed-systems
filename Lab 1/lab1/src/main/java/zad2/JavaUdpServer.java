package zad2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

                String msg = new String(receivePacket.getData(), StandardCharsets.UTF_8);
                System.out.println("received msg: " + trimZeros(msg));


                String response = "żółta gęś";
                sendBuffer = response.getBytes(StandardCharsets.UTF_8);
                System.out.println("UTF-8 encoded bytes: " + new String(sendBuffer, StandardCharsets.UTF_8));

                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, receivePacket.getSocketAddress());
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
