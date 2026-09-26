import RMI.ByteService;
import java.rmi.Naming;

public class QORPF8AZ {
    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "QORPF8AZ";

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
            int minCount = numbers.length;
            for (int number : numbers) {
                int count = 0;
                for (int x : numbers) {
                    if (number == x) {
                        count++;
                    }
                }
                if (count < minCount) {
                    minCount = count;
                    result = number;
                }
            }

            byte[] resultData = {
                    (byte) result,
                    (byte) minCount
            };

            service.submitData(studentCode, qCode, resultData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}