
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class dIGNyShc {

    static final String ENDPOINT =
            "http://36.50.135.242:2221/ObjectService";

    static final String NAMESPACE =
            "http://soap.server.network.judge.dblab.ptit/";

    static final String STUDENT_CODE = "B23DCCE005";
    static final String QUESTION_CODE = "dIGNyShc";

    public static void main(String[] args) {
        try {
            // 1. Nhan danh sach Customer
            List<Customer> customers = requestListCustomer(
                    STUDENT_CODE, QUESTION_CODE);

            System.out.println("Input:");
            for (Customer c : customers) {
                System.out.println(c);
            }

            // 2. Loc khach hang
            List<Customer> result = new ArrayList<>();

            for (Customer c : customers) {
                if (c.purchaseCount >= 6
                        && c.totalSpent.compareTo(
                                new BigDecimal("4000")) >= 0) {
                    result.add(c);
                }
            }

            // 3. Sap xep totalSpent giam dan
            // Neu bang nhau: customerId tang dan
            result.sort((a, b) -> {
                int cmp = b.totalSpent.compareTo(a.totalSpent);

                if (cmp != 0) {
                    return cmp;
                }

                return a.customerId.compareTo(b.customerId);
            });

            System.out.println("\nOutput:");
            for (Customer c : result) {
                System.out.println(c);
            }

            // 4. Gui danh sach ve server
            String status = submitListCustomer(
                    STUDENT_CODE, QUESTION_CODE, result);

            System.out.println("\nStatus: " + status);
            System.out.println("Submission completed.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================================
    // DATA MODEL: Customer
    // =============================================
    static class Customer {
        String customerId;
        int purchaseCount;
        BigDecimal totalSpent;

        Customer(String customerId,
                 int purchaseCount,
                 BigDecimal totalSpent) {

            this.customerId = customerId;
            this.purchaseCount = purchaseCount;
            this.totalSpent = totalSpent;
        }

        @Override
        public String toString() {
            return "Customer{customerId=" + customerId
                    + ", purchaseCount=" + purchaseCount
                    + ", totalSpent="
                    + totalSpent.toPlainString() + "}";
        }
    }

    // =============================================
    // SOAP HELPER
    // =============================================
    static Document callSoap(
            String operation, String parametersXml)
            throws Exception {

        String requestXml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope"
                + " xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:tns=\"" + NAMESPACE + "\">"
                + "<soapenv:Header/>"
                + "<soapenv:Body>"
                + "<tns:" + operation + ">"
                + parametersXml
                + "</tns:" + operation + ">"
                + "</soapenv:Body>"
                + "</soapenv:Envelope>";

        HttpURLConnection conn = (HttpURLConnection)
                new URL(ENDPOINT).openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            conn.setRequestProperty(
                    "Content-Type", "text/xml; charset=utf-8");

            conn.setRequestProperty("SOAPAction", "\"\"");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestXml.getBytes(
                        StandardCharsets.UTF_8));
            }

            int statusCode = conn.getResponseCode();

            InputStream stream =
                    statusCode >= 200 && statusCode < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            if (stream == null) {
                throw new IOException(
                        "HTTP status: " + statusCode);
            }

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false);

            Document response;

            try (InputStream input = stream) {
                response = factory.newDocumentBuilder()
                        .parse(input);
            }

            NodeList faults =
                    response.getElementsByTagNameNS("*", "Fault");

            if (faults.getLength() > 0) {
                throw new IOException(
                        "SOAP Fault: "
                        + faults.item(0).getTextContent());
            }

            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException(
                        "HTTP status: " + statusCode);
            }

            return response;

        } finally {
            conn.disconnect();
        }
    }

    // =============================================
    // XML HELPER
    // =============================================
    static String getChildText(
            Element parent, String name) {

        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node instanceof Element) {
                Element element = (Element) node;

                String localName = element.getLocalName();

                if (localName == null) {
                    localName = element.getNodeName();

                    int colon = localName.indexOf(':');
                    if (colon >= 0) {
                        localName = localName.substring(colon + 1);
                    }
                }

                if (name.equals(localName)) {
                    return element.getTextContent().trim();
                }
            }
        }

        return null;
    }

    // =============================================
    // INPUT: requestListCustomer
    // =============================================
    static List<Customer> requestListCustomer(
            String studentCode,
            String qCode) throws Exception {

        String params =
                "<studentCode>" + escapeXml(studentCode)
                + "</studentCode>"
                + "<qCode>" + escapeXml(qCode)
                + "</qCode>";

        Document response =
                callSoap("requestListCustomer", params);

        List<Customer> customers = new ArrayList<>();

        NodeList elements =
                response.getElementsByTagName("*");

        for (int i = 0; i < elements.getLength(); i++) {
            Node node = elements.item(i);

            if (!(node instanceof Element)) {
                continue;
            }

            Element element = (Element) node;

            String customerId =
                    getChildText(element, "customerId");

            String purchaseCount =
                    getChildText(element, "purchaseCount");

            String totalSpent =
                    getChildText(element, "totalSpent");

            if (customerId != null
                    && purchaseCount != null
                    && totalSpent != null) {

                Customer customer = new Customer(
                        customerId,
                        Integer.parseInt(purchaseCount),
                        new BigDecimal(totalSpent)
                );

                customers.add(customer);
            }
        }

        return customers;
    }

    // =============================================
    // OUTPUT: submitListCustomer
    // =============================================
    static String submitListCustomer(
            String studentCode,
            String qCode,
            List<Customer> customers) throws Exception {

        StringBuilder params = new StringBuilder();

        params.append("<studentCode>")
                .append(escapeXml(studentCode))
                .append("</studentCode>");

        params.append("<qCode>")
                .append(escapeXml(qCode))
                .append("</qCode>");

        for (Customer c : customers) {
            params.append("<data>")
                    .append("<customerId>")
                    .append(escapeXml(c.customerId))
                    .append("</customerId>")
                    .append("<purchaseCount>")
                    .append(c.purchaseCount)
                    .append("</purchaseCount>")
                    .append("<totalSpent>")
                    .append(c.totalSpent.toPlainString())
                    .append("</totalSpent>")
                    .append("</data>");
        }

        Document response = callSoap(
                "submitListCustomer",
                params.toString());

        NodeList elements =
                response.getElementsByTagNameNS("*", "return");

        if (elements.getLength() == 0) {
            return "No status returned";
        }

        return elements.item(0).getTextContent().trim();
    }

    // =============================================
    // XML ESCAPE
    // =============================================
    static String escapeXml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
