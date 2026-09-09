import java.io.*;
import java.net.Socket;

public class uROFxppg {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("36.50.135.242", 2208);
        // Send student code + question code
        BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(client.getOutputStream())
        );
        String studentCode = "B23DCCE005";
        String qCode = "uROFxppg";
        bw.write(studentCode + ";" + qCode);
        bw.newLine();
        bw.flush();
        // Receive data
        BufferedReader br = new BufferedReader(
                new InputStreamReader(client.getInputStream())
        );

        String line = br.readLine();
        String[] arr = line.split(",");
        String res = "";
        for (String x : arr) {
            x = x.trim();
            if (x.endsWith(".edu")) {
                res += x + ", ";
            }
        }
        res = res.substring(0, res.length() - 2);
        bw.write(res);
        bw.flush();
        // Close
        br.close();
        bw.close();
        client.close();
    }
}