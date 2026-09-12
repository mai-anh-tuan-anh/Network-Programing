import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class cfXBFx5T {
    public static void main(String[] args) throws Exception {
        DatagramSocket client = new DatagramSocket();

        //Send data
        String message = ";B23DCCE005;cfXBFx5T";
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("36.50.135.242"), 2208);
        client.send(packet);

        // Receive data
        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        client.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());

        //Process data
        String[] parts = response.split(";");
        String requestID = parts[0].trim();
        String data = parts[1].trim();

        String[] arr = data.split(" ");
        for(int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].substring(0, 1).toUpperCase() + arr[i].substring(1).toLowerCase();
        }
        String result = requestID + ";" + String.join(" ", arr);

        DatagramPacket resultPacket = new DatagramPacket(result.getBytes(), result.length(), InetAddress.getByName("36.50.135.242"), 2208);
        client.send(resultPacket);

        client.close();
    }
}
