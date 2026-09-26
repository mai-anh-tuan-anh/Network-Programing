package RMI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface DataService extends Remote  {
    public Integer[] requestData(String studentCode, String qCode) throws RemoteException;
    public void submitData(String studentCode, String qCode, List<Integer> data) throws RemoteException;
}
