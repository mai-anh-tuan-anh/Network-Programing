import RMI.ByteService;
import java.rmi.Naming;

public class j6XnHOs7 {
    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "j6XnHOs7";
        try {
            ByteService service = (ByteService) Naming.lookup(
                    "rmi://36.50.135.242/RMIByteService"
            );
            byte[] data = service.requestData(studentCode, qCode);
            int[] numbers = new int[data.length];
            for (int i = 0; i < data.length; i++) {
                numbers[i] = data[i];
            }

            int result = numbers[0];
            int maxCount = 0;
            for (int i = 0; i < numbers.length; i++) {
                int count = 0;
                for (int j = 0; j < numbers.length; j++) {
                    if (numbers[i] == numbers[j]) {
                        count++;
                    }
                }
                if (count > maxCount) {
                    maxCount = count;
                    result = numbers[i];
                }
            }

            byte[] resultData = {
                    (byte) result,
                    (byte) maxCount
            };
            service.submitData(studentCode, qCode, resultData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}