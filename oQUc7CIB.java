import RMI.CharacterService;
import java.net.URI;
import java.rmi.Naming;

public class oQUc7CIB {
    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "oQUc7CIB";

        try {
            CharacterService service = (CharacterService) Naming.lookup(
                    "rmi://36.50.135.242/RMICharacterService"
            );

            String s = service.requestCharacter(studentCode, qCode);

            String result = new URI(null, null, s, null).toASCIIString();

            service.submitCharacter(studentCode, qCode, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}