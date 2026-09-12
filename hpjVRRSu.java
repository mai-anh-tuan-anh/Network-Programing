import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class hpjVRRSu{
    public static void main(String[] args) throws Exception {
        DatagramSocket client = new DatagramSocket();

        // Send data
        String message = ";B23DCCE005;hpjVRRSu";
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("36.50.135.242"), 2207);
        client.send(packet);
        
        // Receive data
        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        client.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        System.out.println(response);

        // Process data
        String[] parts = response.split(";");
        String requestID = parts[0];
        String last = parts[1];
        String[] numbers = last.split(",");
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (String num : numbers) {
            int value = Integer.parseInt(num);
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
        }

        // Send response
        String resultMessage = requestID + ";" + max + "," + min;
        DatagramPacket resultPacket = new DatagramPacket(resultMessage.getBytes(), resultMessage.length(), InetAddress.getByName("36.50.135.242"), 2207);
        client.send(resultPacket);

        client.close();
    }
}