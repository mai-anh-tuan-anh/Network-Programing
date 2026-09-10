import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
public class Hzy2GrZN {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("36.50.135.242", 2206);
        InputStream is = client.getInputStream();
        OutputStream os = client.getOutputStream();

        // Send data
        String message = "B23DCCE005;Hzy2GrZN";
        os.write(message.getBytes());
        os.flush();

        //Receive data
        byte[] buffer = new byte[1024];
        is.read(buffer);
        String[] response = new String(buffer).split(",");
        ArrayList<Integer> numbers = new ArrayList<>();
        for (String num : response) {
            numbers.add(Integer.parseInt(num.trim()));
        }

        // Process data
        int max = -1, secondMax = -1, maxIndex = -1, secondMaxIndex = -1;
        for(int i = 0; i < numbers.size(); i++){
            if(numbers.get(i) > max){
                secondMax = max;
                secondMaxIndex = maxIndex;
                
                max = numbers.get(i);
                maxIndex = i;

            } else if(numbers.get(i) > secondMax && numbers.get(i) != max){
                secondMax = numbers.get(i);
                secondMaxIndex = i;
            }
        }
        String res = secondMax + "," + secondMaxIndex;
        os.write(res.getBytes());
        os.flush();

        os.close();
        is.close();
        client.close();
    }
}