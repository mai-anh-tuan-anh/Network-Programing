
import RMI.DataService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class tzqzJy7j {

    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "tzqzJy7j";

        try {
            Registry registry = LocateRegistry.getRegistry(
                    "36.50.135.242", 1099
            );

            DataService service = (DataService)
                    registry.lookup("RMIDataService");

            Object data = service.requestData(studentCode, qCode);

            if (data == null) {
                System.out.println("Server tra ve NULL!");
                return;
            }

            String csv = (String) data;
            System.out.println("Du lieu: " + csv);

            String[] parts = csv.split(",");
            int n = parts.length;

            double[] arr = new double[n];
            BigDecimal sum = BigDecimal.ZERO;

            for (int i = 0; i < n; i++) {
                BigDecimal value = new BigDecimal(parts[i].trim());
                arr[i] = value.doubleValue();
                sum = sum.add(value);
            }

            // Tinh trung binh chinh xac
            BigDecimal avgBD = sum.divide(
                    BigDecimal.valueOf(n),
                    15,
                    RoundingMode.HALF_UP
            );

            double average = avgBD.doubleValue();

            // Tinh do lech chuan tong the
            double variance = 0;
            for (double x : arr) {
                variance += Math.pow(x - average, 2);
            }
            double stddev = Math.sqrt(variance / n);

            // Tinh phan vi 95
            Arrays.sort(arr);
            int index = (int) Math.ceil(n * 0.95) - 1;
            double p95 = arr[index];

            // Lam tron HALF_UP
            String avgStr = avgBD.setScale(
                    2, RoundingMode.HALF_UP
            ).toPlainString();

            String stdStr = BigDecimal.valueOf(stddev)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString();

            String p95Str = BigDecimal.valueOf(p95)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toPlainString();

            String result = "average=" + avgStr
                    + ";stddev=" + stdStr
                    + ";p95=" + p95Str;

            System.out.println("Ket qua: " + result);

            service.submitData(studentCode, qCode, result);

            System.out.println("Da gui ket qua len server!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
