import RMI.ObjectService;
import RMI.TicketSla;

import java.rmi.Naming;

public class q7cAs0tuJ {
    public static void main(String[] args) {
        String studentCode = "B23DCCE005";
        String qCode = "7cAs0tuJ";

        try {
            ObjectService service =
                    (ObjectService) Naming.lookup(
                            "rmi://36.50.135.242/RMIObjectService"
                    );

            TicketSla ticket =
                    (TicketSla) service.requestObject(studentCode, qCode);

            String priority = ticket.getPriority();
            int hours = ticket.getOpenedHoursAgo();

            boolean breached = false;

            switch (priority) {
                case "CRITICAL":
                    breached = hours > 2;
                    break;
                case "HIGH":
                    breached = hours > 8;
                    break;
                case "MEDIUM":
                    breached = hours > 24;
                    break;
                case "LOW":
                    breached = hours > 72;
                    break;
            }

            ticket.setBreached(breached);

            if (!breached) {
                ticket.setAction("MONITOR");
            } else if (priority.equals("CRITICAL") || hours > 96) {
                ticket.setAction("ESCALATE_L2");
            } else {
                ticket.setAction("ESCALATE_L1");
            }

            service.submitObject(studentCode, qCode, ticket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}