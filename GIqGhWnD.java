import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class GIqGhWnD {
    public static void main(String[] args) throws Exception {
        DatagramSocket client = new DatagramSocket();
        // Send data
        String message = ";B23DCCE005;GIqGhWnD";
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("36.50.135.242"), 2208);
        client.send(packet);

        // Receive data
        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        client.receive(responsePacket);
        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());

        // Process data
        String[] parts = response.split(";");
        String requestId = parts[0];
        String data = parts[1];

        HashMap<Character, Integer> dataMap = new HashMap<>();
        int maxCount = 0;
        for (char c : data.toCharArray()) {
            dataMap.put(c, dataMap.getOrDefault(c, 0) + 1);
            maxCount = Math.max(maxCount, dataMap.get(c));
        }
        ArrayList<Integer> indices = new ArrayList<>();
        Character c = null;
        for (int i = 0; i < data.length(); i++){
            if((c == null || data.charAt(i) == c) && (dataMap.get(data.charAt(i)) == maxCount)){
                indices.add(i + 1);
                c = data.charAt(i);
            }
        }
        String res = requestId + ";" + c + ":";
        for (int i = 0; i < indices.size(); i++){
            res += indices.get(i);
            res += ",";
        }

        DatagramPacket responseToSend = new DatagramPacket(res.getBytes(), res.length(), InetAddress.getByName("36.50.135.242"), 2208);
        client.send(responseToSend);
        
        client.close();
    }
}
