import RMI.CharacterService;
import java.rmi.Naming;

public class y15Ydx1c {
    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "y15Ydx1c";

        try {
            CharacterService service = (CharacterService) Naming.lookup(
                    "rmi://36.50.135.242/RMICharacterService"
            );

            String s = service.requestCharacter(studentCode, qCode);
            int number = Integer.parseInt(s);

            int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
            String[] symbols = {
                    "M", "CM", "D", "CD", "C", "XC",
                    "L", "XL", "X", "IX", "V", "IV", "I"
            };

            String roman = "";

            for (int i = 0; i < values.length; i++) {
                while (number >= values[i]) {
                    roman += symbols[i];
                    number -= values[i];
                }
            }

            service.submitCharacter(studentCode, qCode, roman);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}