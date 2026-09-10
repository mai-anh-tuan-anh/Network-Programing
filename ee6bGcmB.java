import TCP.Laptop;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class ee6bGcmB {
    public static void main(String[] args) throws Exception {
        Socket client = new Socket("36.50.135.242", 2209);
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());

        // Send data
        String message = "B23DCCE005;ee6bGcmB";
        oos.writeObject(message);

        // Receive data
        Laptop laptop = (Laptop) ois.readObject();
        String name = laptop.getName().trim();
        StringBuilder quantity = new StringBuilder(String.valueOf(laptop.getQuantity()));

        // Process data;
        String[] arr = name.split(" ");
        String temp = arr[0];
        arr[0] = arr[arr.length - 1];
        arr[arr.length - 1] = temp;
        laptop.setName(String.join(" ", arr));

        quantity.reverse();
        laptop.setQuantity(Integer.parseInt(quantity.toString()));

        oos.writeObject(laptop);

        oos.close();
        ois.close();
        client.close();
    }
}
