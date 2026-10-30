
import RMI.DataService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MU2M7g3r {

    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "MU2M7g3r";

        try {
            Registry registry = LocateRegistry.getRegistry(
                    "36.50.135.242", 1099
            );

            DataService service = (DataService)
                    registry.lookup("RMIDataService");

            Object data = service.requestData(studentCode, qCode);

            if (data == null) {
                System.out.println("Server tra ve NULL!");
                System.out.println("Kiem tra studentCode va qCode.");
                return;
            }

            if (!(data instanceof int[])) {
                System.out.println("Sai kieu du lieu: "
                        + data.getClass().getName());
                return;
            }

            int[] arr = (int[]) data;
            System.out.println("Mang nhan duoc: "
                    + Arrays.toString(arr));

            List<Integer> result = new ArrayList<>();

            for (int i = 0; i < arr.length; i++) {
                boolean left = (i == 0 || arr[i] > arr[i - 1]);
                boolean right = (i == arr.length - 1 || arr[i] > arr[i + 1]);

                if (left && right) {
                    result.add(i + 1);
                }
            }

            System.out.println("Cac vi tri dinh cuc bo: " + result);

            service.submitData(studentCode, qCode, result);

            System.out.println("Da gui ket qua len server!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
