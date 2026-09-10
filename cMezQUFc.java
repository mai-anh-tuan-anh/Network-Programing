import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class cMezQUFc {
    public static void main(String[] args) throws Exception {
        Socket client = new Socket("36.50.135.242", 2207);
        DataInputStream dis = new DataInputStream(client.getInputStream());
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        
        // Send data
        String message = "B23DCCE005;cMezQUFc";
        dos.writeUTF(message);

        // Receive data
        String response = dis.readUTF();
        int n = dis.readInt();
        StringBuilder sb = new StringBuilder();
        for(char c : response.toCharArray()) {
            if(c >= 'A' && c <= 'Z') {
                sb.append((char)('A' + (c - 'A' - n + 26) % 26));
            } else if(c >= 'a' && c <= 'z') {
                sb.append((char)('a' + (c - 'a' - n + 26) % 26));
            } else {
                sb.append(c);
            }
        }
        dos.writeUTF(sb.toString());

        dos.close();
        dis.close();
        client.close();
    }
}
