
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class niMR73cF {
    public static void main(String[] args) throws IOException{
        Socket client = new Socket("36.50.135.242", 2207);
        DataInputStream dis = new DataInputStream(client.getInputStream());
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());

        // Send data
        String message = "B23DCCE005;niMR73cF";
        dos.writeUTF(message);
        dos.flush();
        
        // Receive data
        int a = dis.readInt();
        int b = dis.readInt();

        // Process data
        int sum = a + b;
        int product = a * b;
        dos.writeInt(sum);
        dos.writeInt(product);
        dos.flush();

        dis.close();
        dos.close();
        client.close();
    }
}