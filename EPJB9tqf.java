import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
public class EPJB9tqf {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("36.50.135.242", 2208);
        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

        // Send data
        String message = "B23DCCE005;EPJB9tqf";
        bw.write(message);
        bw.newLine();
        bw.flush();

        // Receive data
        String response = br.readLine();

        // Process data
        LinkedHashMap<Character, Integer> data = new LinkedHashMap<>();
        for(Character x : response.toCharArray()){
            data.put(x, data.getOrDefault(x, 0) + 1);
        }
        String res = "";
        for(Character x : data.keySet()){
            if(data.get(x) > 1 && x != ' '){
                res += x + ":" + data.get(x) + ",";
            }
        }
        bw.write(res);
        bw.newLine();
        bw.flush();

        bw.close();
        br.close();
        client.close();
    }
}