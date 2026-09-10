import TCP.Customer;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GjzeVWLk {
    public static void main(String[] args) throws Exception {
        Socket client = new Socket("36.50.135.242", 2209);
        ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
    
        // Send data
        String message = "B23DCCE005;GjzeVWLk";
        oos.writeObject(message);

        // Receive data
        Customer customer = (Customer) ois.readObject();
        String name = customer.getName();
        String dayOfBirth = customer.getDayOfBirth();

        // Process data
        String[] arr = name.split(" ");
        StringBuilder formattedName = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++) {
            arr[i] = arr[i].substring(0, 1).toUpperCase() + arr[i].substring(1).toLowerCase();
            formattedName.append(arr[i]).append(" ");
        }
        arr[arr.length - 1] = arr[arr.length - 1].toUpperCase();
        formattedName.insert(0, arr[arr.length - 1] + ", ");
        customer.setName(formattedName.toString().trim());

        String[] dateParts = dayOfBirth.split("-");
        String formattedDate = dateParts[1] + "/" + dateParts[0] + "/" + dateParts[2];
        customer.setDayOfBirth(formattedDate);

        StringBuilder userName = new StringBuilder();
        for(int i = 0; i < arr.length - 1; i++) {
            userName.append(arr[i].toLowerCase().charAt(0));
        }
        userName.append(arr[arr.length - 1].toLowerCase());
        customer.setUserName(userName.toString());

        oos.writeObject(customer);

        oos.close();
        ois.close();
        client.close();
    }
}
