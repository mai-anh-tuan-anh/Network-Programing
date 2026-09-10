import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;

public class czv3euUe {
    public static void main(String[] args) throws Exception {
        DatagramSocket client = new DatagramSocket();
        
        // Send data
        String message = ";B23DCCE005;czv3euUe";
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("36.50.135.242"), 2207);
        client.send(packet);

        // Receive data
        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        client.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
        
        // Process data
        String[] parts = response.split(";");
        String requestId = parts[0].trim();
        int n = Integer.parseInt(parts[1].trim());
        String[] nums = parts[2].split(",");
        HashSet<Integer> numsSet = new HashSet<>();
        for (String num : nums) {
            numsSet.add(Integer.parseInt(num.trim()));
        }

        String res = requestId + ";";
        for (int i = 1; i <= n; i++) {
            if (!numsSet.contains(i)) {
                res += i + ",";
            }
        }
        res = res.substring(0, res.length() - 1); // Remove trailing comma
        DatagramPacket resultPacket = new DatagramPacket(res.getBytes(), res.length(), InetAddress.getByName("36.50.135.242"), 2207);
        client.send(resultPacket);

        client.close();
    }
}
