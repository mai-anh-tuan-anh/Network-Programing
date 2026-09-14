
import UDP.Customer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class q6i3yiGQv {
    public static void main(String[] args) throws Exception {
        DatagramSocket client = new DatagramSocket();

        // Send data
        String message = ";B23DCCE005;6i3yiGQv";
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("36.50.135.242"), 2209);
        client.send(packet);

        // Receive data
        byte[] bytes = new byte[4096];
        DatagramPacket responsePacket = new DatagramPacket(bytes, bytes.length);
        client.receive(responsePacket);

        String requestID = new String(responsePacket.getData(), 0, 8);
        ByteArrayInputStream bais = new ByteArrayInputStream(responsePacket.getData(), 8, responsePacket.getLength() - 8);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Customer customer = (Customer) ois.readObject();

        // Process data
        String name = customer.getName();
        String dob = customer.getDayOfBirth();
        String[] arr = name.split(" ");

        // a
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++){
            sb.append(arr[i].substring(0,1).toUpperCase() + arr[i].substring(1).toLowerCase() + " ");
        }
        sb.insert(0, arr[arr.length - 1].toUpperCase() + ", ");
        customer.setName(sb.toString().trim());

        // b
        String[] Date = dob.split("-");
        customer.setDayOfBirth(Date[1] + "/" + Date[0] + "/" + Date[2]);

        // c
        sb = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++){
            sb.append(arr[i].toLowerCase().charAt(0));
        }
        sb.append(arr[arr.length - 1].toLowerCase());
        customer.setUserName(sb.toString().trim());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(customer);
        oos.flush();
        
        byte[] requestIdBytes = requestID.getBytes();
        byte[] customerBytes = baos.toByteArray();
        byte[] data = new byte[8 + customerBytes.length];
        System.arraycopy(requestIdBytes, 0, data, 0, 8);
        System.arraycopy(customerBytes,  0, data, 8, customerBytes.length);

        DatagramPacket result = new DatagramPacket(data, data.length, InetAddress.getByName("36.50.135.242"), 2209);
        client.send(result);

        client.close();
    }
}
