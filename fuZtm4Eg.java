import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class fuZtm4Eg {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("36.50.135.242", 2206);

        InputStream is = client.getInputStream();
        OutputStream os = client.getOutputStream();

        // a. Gửi studentCode;qCode
        String studentCode = "B23DCCE005";
        String qCode = "fuZtm4Eg";

        String request = studentCode + ";" + qCode;
        os.write(request.getBytes());
        os.flush();

        // b. Nhận chuỗi số
        byte[] buffer = new byte[1024];
        int len = is.read(buffer);

        String data = new String(buffer, 0, len);

        String[] arr = data.split(",");

        ArrayList<Integer> nums = new ArrayList<>();

        for (String x : arr) {
            nums.add(Integer.parseInt(x.trim()));
        }

        // c. Tìm khoảng cách nhỏ nhất
        nums.sort(Integer::compareTo);

        int distance = 1_000_000_007;
        int num1 = -1;
        int num2 = -1;

        for (int i = 0; i < nums.size() - 1; i++) {
            int currentDistance = nums.get(i + 1) - nums.get(i);

            if (currentDistance < distance) {
                distance = currentDistance;
                num1 = nums.get(i);
                num2 = nums.get(i + 1);
            }
        }

        String result = distance + "," + num1 + "," + num2;

        os.write(result.getBytes());
        os.flush();

        // d. Đóng
        client.close();
    }
}