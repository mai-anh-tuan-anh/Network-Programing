import RMI.DataService;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

public class MU2M7g3r {
    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "MU2M7g3r";

        try {
            DataService service = (DataService) Naming.lookup(
                    "rmi://36.50.135.242:1099/RMIDataService"
            );

            Integer[] data = service.requestData(studentCode, qCode);

            List<Integer> result = new ArrayList<Integer>();

            for (int i = 0; i < data.length; i++) {
                if (i == 0) {
                    if (data[i] > data[i + 1]) {
                        result.add(i + 1);
                    }
                }
                else if (i == data.length - 1) {
                    if (data[i] > data[i - 1]) {
                        result.add(i + 1);
                    }
                }
                else {
                    if (data[i] > data[i - 1] && data[i] > data[i + 1]) {
                        result.add(i + 1);
                    }
                }
            }

            service.submitData(studentCode, qCode, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}