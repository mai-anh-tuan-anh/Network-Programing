import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import UDP.Student;

public class Uft7BuJX {
    public static void main(String[] args) throws Exception {
        DatagramSocket client = new DatagramSocket();

        // Send data
        String message = ";B23DCCE005;Uft7BuJX";
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName("36.50.135.242"), 2209);
        client.send(packet);

        // Receive data
        byte[] buffer = new byte[4096];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        client.receive(responsePacket);
        String requestId = new String(responsePacket.getData(), 0, 8);
        ByteArrayInputStream bis = new ByteArrayInputStream(
        responsePacket.getData(),
        8,
        responsePacket.getLength() - 8);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Student student = (Student) ois.readObject();

        //Process data
        String name = student.getName();
        System.err.println(name);
        String[] arr = name.split(" ");
        for(int i = 0; i < arr.length; i++){
            arr[i] = arr[i].substring(0,1).toUpperCase() + arr[i].substring(1).toLowerCase();
        }
        student.setName(String.join(" ", arr));

        StringBuilder email = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++){
            email.append(arr[i].substring(0,1).toLowerCase());
        }
        email.append("@ptit.edu.vn");
        email.insert(0, arr[arr.length - 1].toLowerCase());
        student.setEmail(email.toString());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(student);
        oos.flush();

        byte[] studentBytes = bos.toByteArray();
        byte[] requestIdBytes = requestId.getBytes();

        byte[] data = new byte[8 + studentBytes.length];

        System.arraycopy(requestIdBytes, 0, data, 0, 8);
        System.arraycopy(studentBytes, 0, data, 8, studentBytes.length);

        DatagramPacket resPacket = new DatagramPacket(data, data.length,InetAddress.getByName("36.50.135.242"), 2209);
        client.send(resPacket);

        client.close();
    }
}
